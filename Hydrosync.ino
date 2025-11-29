#include <BLEDevice.h>
#include <BLEUtils.h>
#include <BLEServer.h>
#include <BLE2902.h>
#include <Wire.h> 
#include <MAX30100_PulseOximeter.h> 


#define SDA_PIN 21       // I2C SDA
#define SCL_PIN 22       // I2C SCL
#define GSR_PIN 34       // Analog Input (GSR)
#define LM35_PIN 32      // Analog Input (Temp)

#define REPORTING_PERIOD_MS 1000 
#define SERVICE_UUID        "0000ffe0-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID "0000ffe1-0000-1000-8000-00805f9b34fb"

BLECharacteristic *pCharacteristic;
PulseOximeter pox;
bool deviceConnected = false;
unsigned long lastReportTime = 0;

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("App Connected!");
    }
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("App Disconnected. Restarting Scan...");
      BLEDevice::startAdvertising(); 
    }
};

void setup() {
  Serial.begin(115200);

  Wire.begin(SDA_PIN, SCL_PIN);
  analogReadResolution(12); 

  if (!pox.begin()) {
    Serial.println("FAILED: MAX30100 not found! Check wiring.");
  } else {
    Serial.println("MAX30100 Initialized.");
    pox.setIRLedCurrent(PulseOximeter::MAX_IR_CURRENT); 
  }
  
  BLEDevice::init("HydroSync_Wearable"); 
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService(SERVICE_UUID);
  pCharacteristic = pService->createCharacteristic(
    CHARACTERISTIC_UUID,
    BLECharacteristic::PROPERTY_NOTIFY
  );
  pCharacteristic->addDescriptor(new BLE2902());
  pService->start();

  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  BLEDevice::startAdvertising();
  
  Serial.println("HydroSync Ready & Advertising...");
}

void loop() {

  pox.update();

  if (millis() - lastReportTime > REPORTING_PERIOD_MS) { 
  
    int rawGSR = analogRead(GSR_PIN);
    
    float tempC = (analogRead(LM35_PIN) * 3300.0 / 4095.0) / 10.0;
    
    float heartRate = pox.getHeartRate(); 

    Serial.print("HR: "); Serial.print(heartRate);
    Serial.print(" | Temp: "); Serial.print(tempC);
    Serial.print(" | GSR: "); Serial.println(rawGSR);

   
    if (deviceConnected) {
        uint8_t packet[9];

        
        packet[0] = 0xAA; packet[1] = 0xAA; packet[2] = 0xAA; packet[3] = 0xAA;

   
        packet[4] = (uint8_t)heartRate;

        packet[5] = (uint8_t)(rawGSR & 0xFF);
        packet[6] = (uint8_t)((rawGSR >> 8) & 0xFF);

        
        int16_t tempScaled = (int16_t)(tempC * 100);
        packet[7] = (uint8_t)(tempScaled & 0xFF);
        packet[8] = (uint8_t)((tempScaled >> 8) & 0xFF);

      
        pCharacteristic->setValue(packet, 9);
        pCharacteristic->notify();
    }
    
    lastReportTime = millis();
  }
}
