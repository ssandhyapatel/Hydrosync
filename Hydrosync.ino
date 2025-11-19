#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <Wire.h> 
#include <MAX30100_PulseOximeter.h> 


#define SDA_PIN 21
#define SCL_PIN 22


#define GSR_PIN 34    
#define LM35_PIN 32   

#define LM35_ADC_SCALE (3300.0 / 4095.0) 
#define LM35_CONVERSION_FACTOR (LM35_ADC_SCALE / 10.0) 


PulseOximeter pox;

#define REPORTING_PERIOD_MS 100 


#define DEVICE_NAME         "HydroSync_Wearable"
#define SERVICE_UUID        "0000ffe0-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID "0000ffe1-0000-1000-8000-00805f9b34fb"

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;
unsigned long lastReportTime = 0;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("BLE Client Connected.");
    }

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("BLE Client Disconnected. Starting advertising...");
      BLEDevice::startAdvertising();
    }
};


//  SENSOR READING 


float readSkinTemperature() {

  int rawADC = analogRead(LM35_PIN);
  

  float temperatureC = (float)rawADC * LM35_CONVERSION_FACTOR; 
  return temperatureC;
}

int readGSR() {

  int rawGSR = analogRead(GSR_PIN);

  return rawGSR;
}


void setup() {
  Serial.begin(115200);

  Wire.begin(SDA_PIN, SCL_PIN);


  analogReadResolution(12);

  if (!pox.begin()) {
    Serial.println("FATAL: PPG/HRV Sensor (MAX30100) not found or failed initialization.");
  } else {
    Serial.println("MAX30100 initialized successfully.");

    pox.setIRLedCurrent(PulseOximeter::MAX_IR_CURRENT); 
  }
  
  // BLE
  BLEDevice::init(DEVICE_NAME); 
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);

  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_NOTIFY
  );

  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();

  //  Advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  BLEDevice::startAdvertising();

  Serial.println(String(DEVICE_NAME) + " is advertising...");
}

void loop() {

  pox.update();

  if (deviceConnected && (millis() - lastReportTime > 2000)) { 
  
    float temp = readSkinTemperature(); 
    int gsrRaw = readGSR();             
    float heartRate = pox.getHeartRate(); 

    String data = String(heartRate, 1) + "," + 
                  String(temp, 2) + "," + 
                  String(gsrRaw);

    // Print
    Serial.println("--- HydroSync Data Packet ---");
    Serial.println("HR: " + String(heartRate, 1) + " BPM");
    Serial.println("Temp: " + String(temp, 2) + " C");
    Serial.println("GSR: " + String(gsrRaw) + " raw");
    
    //  Notify 
    pCharacteristic->setValue(data.c_str());
    pCharacteristic->notify();
    
    lastReportTime = millis();
  }

  if (!deviceConnected) {
    delay(500); 
  }
}
