#include <WiFi.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include "MAX30105.h" // Using the SparkFun library from the Health Monitor code
#include "heartRate.h" // Often included with SparkFun library
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// ================== USER CONFIG ==================
const char* SSID     = "YOUR_WIFI_NAME";      
const char* PASSWORD = "YOUR_WIFI_PASSWORD";  
const char* HOST_IP  = "192.168.1.100";        
const int HOST_PORT  = 5005;
const char* DEVICE_ID = "hydrosync_01";

// ================== PINS & HARDWARE ==================
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
#define SDA_PIN 21
#define SCL_PIN 22
#define GSR_PIN 34       // Analog Input (GSR)
#define LM35_PIN 32      // Analog Input (Temp)

// ================== BLE DEFS ==================
#define SERVICE_UUID        "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"

// ================== OBJECTS ==================
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);
MAX30105 particleSensor;
WiFiUDP Udp;
BLECharacteristic* pCharacteristic;

// ================== GLOBALS ==================
bool deviceConnected = false;
unsigned long packetCount = 0;

// Graphing
byte graphData[128];
int graphIdx = 0;

// Signal Processing
float irDC = 0;
float irAC = 0;
long lastBeatTime = 0;
float currentBPM = 0;

// HRV Calculation (RMSSD)
#define RR_BUFFER_SIZE 20
long rrIntervals[RR_BUFFER_SIZE];
int rrIndex = 0;
float currentHRV = 0; // RMSSD

// Timers
unsigned long lastUdpSend = 0;
unsigned long lastBLE = 0;

// ================== BLE CALLBACKS ==================
class MyServerCallbacks: public BLEServerCallbacks {
  void onConnect(BLEServer*) { deviceConnected = true; }
  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
    pServer->getAdvertising()->start();
  }
};

// ================== SETUP ==================
void setup() {
  Serial.begin(115200);
  
  // 1. Init Pins
  Wire.begin(SDA_PIN, SCL_PIN);
  analogReadResolution(12); // 0-4095

  // 2. Init OLED
  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
     Serial.println(F("SSD1306 allocation failed")); 
     for(;;);
  }
  display.clearDisplay();
  display.setTextColor(WHITE);
  display.println("HydroSync Init...");
  display.display();

  // 3. Init MAX30105
  if (!particleSensor.begin(Wire, I2C_SPEED_FAST)) {
    display.println("MAX30105 FAIL");
    display.display();
    while (1);
  }
  // Setup for High Sensitivity
  particleSensor.setup(60, 4, 2, 100, 411, 4096); 
  particleSensor.setPulseAmplitudeGreen(0);

  // 4. Init BLE
  BLEDevice::init("Sandhya-ESP32"); 
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();
  pServer->getAdvertising()->start();

  // 5. Init WiFi (Optional - Code continues if fails to connect instantly to allow offline use)
  WiFi.begin(SSID, PASSWORD);
  Serial.print("Connecting to WiFi");
  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 10) {
    delay(500);
    Serial.print(".");
    retry++;
  }
  if(WiFi.status() == WL_CONNECTED) {
     Serial.println("\nWiFi Connected");
     display.println("WiFi OK");
  } else {
     display.println("WiFi Failed");
  }
  display.display();
  delay(1000);

  resetGraph();
}

// ================== LOOP ==================
void loop() {
  // 1. Read Raw IR for Graph
  long ir = particleSensor.getIR();

  // FINGER CHECK
  if (ir < 50000) { // Threshold for finger detection
    currentBPM = 0;
    currentHRV = 0;
    resetGraph();
    drawScreen(0, 0, 0, false);
    return;
  }

  // 2. Signal Processing (DC Removal)
  irDC = irDC + 0.05 * (ir - irDC); // Stronger filter
  irAC = ir - irDC;
  irAC = constrain(irAC, -2000, 2000); // Clamp for graph

  // 3. Beat Detection & HRV Logic
  detectBeat(irAC);

  // 4. Update OLED Graph
  updateGraph(irAC);

  // 5. Periodically Read Sensors & Send Data (Every 50ms)
  if (millis() - lastUdpSend > 50) {
    
    // Read Analog Sensors
    int rawGSR = analogRead(GSR_PIN);
    float tempC = (analogRead(LM35_PIN) * 3300.0 / 4095.0) / 10.0;
    
    // Update Display
    drawScreen(tempC, rawGSR, currentHRV, true);

    // Send UDP
    if(WiFi.status() == WL_CONNECTED) {
       sendUdpPacket(currentBPM, tempC, rawGSR, currentHRV);
    }

    // Send BLE (Throttled to 50ms to match UDP)
    sendBLE(currentBPM, tempC, rawGSR, currentHRV);

    lastUdpSend = millis();
  }
}

// ================== HELPER FUNCTIONS ==================

void detectBeat(float signal) {
  // Simple Peak Detection
  // If signal crosses threshold and enough time has passed
  if (signal > 300 && (millis() - lastBeatTime > 250)) {
    long now = millis();
    long rrInterval = now - lastBeatTime;
    lastBeatTime = now;

    // Calculate BPM
    float bpm = 60000.0 / rrInterval;
    if (bpm > 40 && bpm < 180) {
      currentBPM = 0.9 * currentBPM + 0.1 * bpm; // Smoothing
      
      // --- HRV CALCULATION (RMSSD) ---
      // 1. Push new RR to buffer
      rrIntervals[rrIndex] = rrInterval;
      rrIndex = (rrIndex + 1) % RR_BUFFER_SIZE;

      // 2. Calculate RMSSD
      double sumSquaredDiffs = 0;
      int count = 0;
      for (int i = 0; i < RR_BUFFER_SIZE - 1; i++) {
        // Only calculate if buffer is filled with valid data (>0)
        if (rrIntervals[i] > 0 && rrIntervals[i+1] > 0) {
          long diff = rrIntervals[i] - rrIntervals[i+1];
          sumSquaredDiffs += (diff * diff);
          count++;
        }
      }
      if (count > 0) {
        currentHRV = sqrt(sumSquaredDiffs / count);
      }
      // -------------------------------
    }
  }
}

void updateGraph(float val) {
  // Map value to screen height (0-63)
  int y = map((int)val, -2000, 2000, 63, 15); // Keep top 15px clear for text
  y = constrain(y, 15, 63);
  graphData[graphIdx] = y;
  graphIdx = (graphIdx + 1) % 128;
}

void resetGraph() {
  for (int i = 0; i < 128; i++) graphData[i] = 40; // Flatline
}

void drawScreen(float temp, int gsr, float hrv, bool fingerDetected) {
  display.clearDisplay();

  // Draw Graph
  if (fingerDetected) {
    for (int i = 0; i < 127; i++) {
      int i1 = (graphIdx + i) % 128;
      int i2 = (graphIdx + i + 1) % 128;
      display.drawLine(i, graphData[i1], i + 1, graphData[i2], WHITE);
    }
  }

  // Draw Header UI
  display.fillRect(0, 0, 128, 16, BLACK); // Background for text
  display.setTextColor(WHITE);
  display.setTextSize(1);
  
  // Left: BPM
  display.setCursor(0, 0);
  display.print("BPM:"); display.print((int)currentBPM);

  // Center: HRV
  display.setCursor(50, 0);
  display.print("HRV:"); display.print((int)hrv);

  // Right: Connection Status
  display.setCursor(110, 0);
  if (deviceConnected) display.print("BT");
  else if (WiFi.status() == WL_CONNECTED) display.print("WF");

  // Bottom overlay for Temp/GSR (Optional, minimal)
  display.setCursor(0, 55);
  // display.print("T:"); display.print((int)temp); 
  
  display.display();
}

void sendUdpPacket(float hr, float temp, int gsr, float hrv) {
  char buffer[512]; 
  snprintf(buffer, sizeof(buffer), 
    "{\"id\":\"%s\",\"seq\":%lu,\"bpm\":%.1f,\"hrv\":%.1f,\"temp\":%.1f,\"gsr\":%d}",
    DEVICE_ID, packetCount++, hr, hrv, temp, gsr
  );
  Udp.beginPacket(HOST_IP, HOST_PORT);
  Udp.print(buffer);
  Udp.endPacket();
}

void sendBLE(float hr, float temp, int gsr, float hrv) {
  if (deviceConnected) {
    char bleBuffer[64];
    // Sending a simple CSV string or JSON via BLE Notify
    snprintf(bleBuffer, sizeof(bleBuffer), "%.1f,%.1f,%.1f,%d", hr, hrv, temp, gsr);
    pCharacteristic->setValue((uint8_t*)bleBuffer, strlen(bleBuffer));
    pCharacteristic->notify();
  }
}
