HydroSync
HydroSync is a comprehensive physiological monitoring system designed to track real-time health metrics such as hydration and fatigue. It combines a custom ESP32-based wearable device with a feature-rich Android application to provide users with actionable wellness insights.

📖 Table of Contents
Overview

Key Features

System Architecture

Tech Stack

Getting Started

Hardware Setup

Mobile App Setup

Usage

Documentation

Author

🧐 Overview
The HydroSync system addresses the need for continuous physiological monitoring. The wearable device acquires data from multiple sensors and transmits it via Bluetooth Low Energy (BLE) to the mobile app. The app processes this data to calculate a "Hydration Score" and "Fatigue Level," visualizing trends and alerting the user to critical states like dehydration or high stress.

✨ Key Features
Wearable Device
Multi-Sensor Fusion: Integrates Galvanic Skin Response (GSR), Heart Rate Variability (HRV), and Skin Temperature sensors.

Real-Time Data: Samples physiological data at user-configurable intervals (e.g., 5s, 10s, 30s).

Efficient Connectivity: Uses Bluetooth Low Energy (BLE) for low-latency (< 2s) data transmission.

Portable Power: Runs on a rechargeable Li-Ion battery for ≥ 5 hours of continuous use.

Mobile Application
Live Dashboard: Displays real-time hydration status, fatigue levels, and temperature with color-coded status banners (e.g., "Optimal", "Dehydrated").

Smart Alerts: Detects and logs critical events (e.g., "Fatigue Detected") with timestamped history and export capabilities.

Trend Analysis: Visualizes historical data (Daily, Weekly, Monthly) to identify long-term health patterns.

Offline Capability: Stores data locally using SQLite, allowing access to trends without an internet connection.

🏗️ System Architecture
The project is divided into four main subsystems:

Wearable Hardware: ESP32 MCU, Sensors (GSR, HRV, Temp), and Battery.

Wearable Firmware: Arduino-based software handling sensor acquisition and BLE broadcasting.

Communication: BLE protocol for data packets (Timestamp, GSR, HRV, Temp, Battery).

Mobile Application: Android app built with MVVM architecture (Model-View-ViewModel).

💻 Tech Stack
Hardware
Microcontroller: ESP32 (Wi-Fi/BLE capable).

Sensors:

GSR Sensor (Skin Conductivity)

Pulse/HRV Sensor (e.g., MAX30100)

Temperature Sensor (e.g., LM35)

Power: Li-Ion Battery.

Software
Firmware: C++ / Arduino IDE.

Mobile App: Kotlin / Android Studio.

Architecture: MVVM.

UI Design: Figma-based layouts.

🚀 Getting Started
Prerequisites
Hardware: ESP32 development board, sensor modules, and jumper wires.

Software:

Arduino IDE (for firmware).

Android Studio (for mobile app).

Hardware Setup
Connect the sensors to the ESP32 pins as defined in the hardware design (refer to HS-DES-002).

Open Hydrosync.ino in the Arduino IDE.

Install necessary libraries for the ESP32 and sensors.

Upload the sketch to your ESP32 board.

Mobile App Setup
Clone this repository.

Open the hydrosync folder in Android Studio.

Sync the project with Gradle files.

Build and Run the application on an Android device or emulator (ensure Bluetooth is enabled).

📱 Usage
Power On: Turn on the wearable device.

Pairing: Open the HydroSync app and navigate to the connection screen to scan for the device.

Dashboard: Once connected, view your real-time metrics on the Home screen.

Settings: Go to the User Profile or Settings tab to configure alert thresholds and sync intervals.

History: Check the Trends tab to see how your hydration and fatigue have changed over time.

📂 Documentation
Detailed documentation can be found in the Documentation/ directory:

HS-REQ-001: Product Requirements Document.

HS-DES-001: System Architecture Document.

HS-DES-004: Mobile Application Design Document.

HS-VVP-001: Verification & Validation Plan.

✍️ Author
Sandhya Patel
