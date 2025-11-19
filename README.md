# HydroSync

A real-time physiological monitoring system for hydration, stress, and fatigue tracking using an ESP32-based wearable and an Android mobile application.

## 📌 Overview

HydroSync is a multi-sensor wearable + mobile app system designed to continuously monitor:

* **GSR (Skin Conductance)**
* **HRV / Heart Rate**
* **Skin Temperature**

The wearable streams data via **Bluetooth Low Energy (BLE)** to the Android application, which processes, visualizes, and logs the metrics. The system is intended for daily use, sports performance monitoring, and mental/physical stress tracking.

---

## 🚀 Features

### **Wearable Device**

* ESP32-based modular sensor platform
* Real-time BLE data streaming
* 3 key physiological sensors: GSR, HRV, Temperature
* Low-power design with rechargeable Li-ion battery

### **Mobile App**

* Real-time dashboard with line charts
* Hydration & fatigue insights
* BLE scan + connect workflow
* Data logging + history/trend analysis
* Smart alerts for dehydration & stress

---

## 🏗 System Architecture

```
Sensors (GSR / HRV / Temp)
          ↓
       ESP32 MCU → Preprocessing → BLE Packet
          ↓
      Android App → Parsing → UI → Storage → Alerts
```

---

## 🛠 Technology Stack

### **Hardware**

* ESP32 Dev Board
* GSR Sensor
* Pulse/HRV Sensor
* Skin Temperature Sensor

### **Firmware**

* Arduino IDE / ESP32 Core
* BLE Server (Custom Characteristic)

### **Mobile App**

* Kotlin + Android Studio
* MVVM Architecture
* LiveData & Coroutines
* MPAndroidChart for real-time graphs
* SQLite local storage

---

## 📥 Getting Started

### **1. Clone Repository**

```bash
git clone https://github.com/ssandhyapatel/Hydrosync.git
```

### **2. Hardware Setup**

* Connect GSR, HRV, and Temp sensors to ESP32 pins
* Upload provided firmware via Arduino IDE
* Power device (USB or Li-ion battery)
* Ensure BLE is broadcasting

### **3. App Setup (Android Studio)**

* Open project in Android Studio
* Allow Gradle to sync
* Build + run app on a BLE-supported device

---

## 📱 Usage Flow

1. Power the wearable device
2. Open HydroSync app
3. Scan → Connect to ESP32 BLE device
4. View real-time sensor graphs
5. Review hydration/fatigue status
6. Check history & logs

---

## 📚 Documentation

This repository includes design docs:

* PRD (Requirements)
* System Architecture Notes
* App UI/UX Design Specs
* BLE Characteristic Documentation

---

## 🤝 Contributing

Contributions welcomed!

1. Fork repo
2. Create a feature branch
3. Commit changes
4. Submit Pull Request

---

## 📄 License

MIT License — free to use, modify, and distribute.

---

## 👤 Author

**Sandhya Patel**

HydroSync — Stay Hydrated, Stay Healthy! 🌊

