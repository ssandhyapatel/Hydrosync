#include <WiFi.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include "MAX30100_PulseOximeter.h" 


const char* SSID     = "YOUR_WIFI_NAME";      
const char* PASSWORD = "YOUR_WIFI_PASSWORD";  
const char* HOST_IP  = "192.168.1.100";       
const int HOST_PORT  = 5005;
const char* DEVICE_ID = "hydrosync_01";



#define SDA_PIN 21
#define SCL_PIN 22
#define GSR_PIN 34       // Analog Input (GSR)
#define LM35_PIN 32      // Analog Input (Temp)


PulseOximeter pox;
WiFiUDP Udp;

// --- TIMERS ---
unsigned long lastUdpSend = 0;
unsigned long packetCount = 0;

//  CALLBACK 

void onBeatDetected() {
    
}

void setup() {
  Serial.begin(115200);

  // 1. Initialize Sensors
  Wire.begin(SDA_PIN, SCL_PIN);
  analogReadResolution(12); // ESP32 default is 12-bit (0-4095)
  
  Serial.print("Initializing MAX30100...");
  if (!pox.begin()) {
    Serial.println("FAILED: MAX30100 not found!");
    Serial.println("Check wiring: SDA->21, SCL->22");
    
  } else {
    Serial.println("SUCCESS");
    pox.setIRLedCurrent(PulseOximeter::MAX_IR_CURRENT);
    pox.setOnBeatDetectedCallback(onBeatDetected);
  }


  Serial.print("Connecting to WiFi: ");
  Serial.println(SSID);
  WiFi.begin(SSID, PASSWORD);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");
  Serial.print("Device IP: "); Serial.println(WiFi.localIP());
  Serial.print("Target IP: "); Serial.println(HOST_IP);
}

void loop() {
 
  pox.update(); 

  if (millis() - lastUdpSend > 50) {
    
    // 1. Read Analog Sensors
    int rawGSR = analogRead(GSR_PIN);
    
  
    float tempC = (analogRead(LM35_PIN) * 3300.0 / 4095.0) / 10.0;
    
   
    float heartRate = pox.getHeartRate(); 
    float spo2 = pox.getSpO2(); 

  
    sendPacket(heartRate, tempC, rawGSR, spo2);
    
    lastUdpSend = millis();
  }
}

void sendPacket(float hr, float temp, int gsr, float spo2) {
  char buffer[512]; 
  unsigned long now = millis();
  
  snprintf(buffer, sizeof(buffer), 
    "{\"device_id\":\"%s\",\"stream\":\"wearable\",\"sample_index\":%lu,\"timestamp_monotonic_ms\":%lu,\"values\":[%.2f,%.2f,%d,%.1f]}",
    DEVICE_ID, packetCount++, now, hr, temp, gsr, spo2
  );


  Udp.beginPacket(HOST_IP, HOST_PORT);
  Udp.print(buffer);
  Udp.endPacket();
}
