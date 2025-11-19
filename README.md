HydroSync
Overview
HydroSync is a comprehensive physiological monitoring system designed to track key health metrics such as hydration and fatigue. The system consists of a custom wearable device that acquires sensor data and transmits it via Bluetooth Low Energy (BLE) to a companion mobile application for analysis, visualization, and user feedback.

Key Features
Wearable Device
Multi-Sensor Integration: Incorporates sensors for Galvanic Skin Response (GSR), Heart Rate Variability (HRV), and peripheral skin temperature.

Real-Time Processing: Powered by an ESP32 microcontroller for data acquisition and control.

Wireless Connectivity: Utilizes Bluetooth Low Energy (BLE) for efficient data transmission with a latency of ≤ 2 seconds.

Battery Operated: Designed for continuous operation using a rechargeable Lithium-Ion battery.

Mobile Application
Live Dashboard: Displays real-time metrics including hydration status, fatigue levels, and skin temperature, along with a color-coded primary status banner (e.g., "Optimal," "Dehydrated").

Trend Analysis: Visualizes historical physiological data over daily, weekly, or monthly periods to help users identify patterns.

Smart Alerts: Logs and displays alert events (e.g., "Fatigue Detected") with timestamps and severity levels, allowing users to filter or export history.

User Personalization: Includes user profiles with configurable alert thresholds and synchronization intervals.

System Architecture
The HydroSync system is composed of four primary subsystems:

Wearable Hardware Subsystem: Physical components including the ESP32 MCU and sensors (GSR, Pulse/HRV, LM35 Temp).

Wearable Firmware Subsystem: Software running on the ESP32 (developed in Arduino IDE) that handles sensor reading, preprocessing, and BLE broadcasting.

Communication Subsystem: Defines the BLE protocol for data transfer between the wearable and mobile app within a 10-meter range.

Mobile Application Subsystem: User-facing Android application built with an MVVM architecture to process data and provide actionable wellness recommendations.

Technical Specifications
Hardware
Microcontroller: ESP32

Sensors:

GSR (Galvanic Skin Response) for hydration monitoring

MAX30100 (or similar PPG-based sensor) for HRV/Pulse

LM35 (or similar) for Skin Temperature

Power: Rechargeable Li-Ion Battery (≥ 5 hours continuous operation)

Software
Firmware Development: Arduino IDE

Mobile Platform: Android

App Architecture: Model-View-ViewModel (MVVM)

Local Storage: SQLite database for offline trend tracking

UI Design: Based on Figma mockups

Getting Started
Prerequisites
Hardware: ESP32 development board, required sensors, and wiring/soldering equipment.

Software:

Arduino IDE for flashing the firmware.

Android Studio (implied for Android development) for building the mobile app.

Configuration
Sync Interval: The data synchronization interval is user-configurable via the mobile app settings (e.g., 5s, 10s, 30s).

Alert Thresholds: Users can set custom thresholds for hydration, fatigue, and temperature alerts.

Author
Sandhya Patel
