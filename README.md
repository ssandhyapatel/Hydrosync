HydroSync Mobile Application
HydroSync is a native Android application designed to monitor hydration, fatigue, and stress levels in real-time using Bluetooth Low Energy (BLE) sensors. The app combines real-time sensor data with on-device Machine Learning (TensorFlow Lite) to provide personalized wellness insights, trends, and urgent alerts.

📱 Features
1. Real-Time Monitoring
Live Dashboard: Visualizes real-time Hydration and Fatigue percentages using circular progress bars.

Sensor Data: Connects to custom BLE hardware to read Heart Rate, Galvanic Skin Response (GSR), and Temperature.

Connectivity: Auto-reconnect functionality and a dedicated background foreground service to keep the connection alive.

2. AI-Powered Insights
On-Device Inference: Uses a TensorFlow Lite model (model.tflite) to predict wellness states based on sensor inputs.

Personal Summary: Displays confidence levels and overall wellness status (Excellent, Fair, Low).

3. Trends & Analytics
Historical Charts: View hydration and fatigue trends over the last 24 hours (Day view) or 7 days (Week view).

Data Export: Export sensor logs to CSV for external analysis.

4. Smart Alerts & Wellness
Urgent Alerts: Notifications for high stress or fatigue levels (e.g., "High Fatigue Warning").

Reminders: Configurable "Daily Nudge" and "Smart Morning" reminders powered by WorkManager.

Hydration Logging: Simple interface to log water intake.

5. Developer Tools
Mock Data Seeder: Built-in "Demo Mode" in Settings to generate 24 hours of realistic mock data for testing UI and charts without hardware.

🛠 Tech Stack
Language: Kotlin

Minimum SDK: 26 (Android 8.0)

Target SDK: 34 (Android 14)

Architecture: MVVM (Model-View-ViewModel)

Dependency Injection: Dagger Hilt

Asynchronous Programming: Kotlin Coroutines & Flow

Local Storage: Room Database

Background Processing: WorkManager & Foreground Services

Machine Learning: TensorFlow Lite

Charting: MPAndroidChart

UI: XML Layouts with ViewBinding

Build System: Gradle (Kotlin DSL)

📂 Project Structure
Plaintext

com.hydrosync.mobile
├── ble/                # Bluetooth logic (Manager, Scanner, Service)
├── data/               # Room Entities and DAOs (Sensor, Alert, Prediction)
├── di/                 # Hilt Dependency Injection Modules
├── ml/                 # TensorFlow Lite wrappers and Inference Workers
├── notifications/      # Notification Channel helpers
├── onboarding/         # Intro screens and permission handling
├── repo/               # Repositories (Single source of truth for data)
├── settings/           # User preferences (DataStore/SharedPreferences)
├── ui/                 # Activities, Fragments, ViewModels, Adapters
│   ├── alerts/         # Alert listing and management
│   ├── dashboard/      # Main real-time view
│   ├── personal/       # AI Summary view
│   ├── trends/         # Historical charts
│   └── ...
├── util/               # Utilities (Mock Data Seeder)
└── wellness/           # Drink logging and reminder logic
🚀 Getting Started
Prerequisites
Android Studio Iguana or later.

Physical Android device (BLE features do not work on standard emulators).

Installation
Clone the repository:

Bash

git clone https://github.com/your-username/hydrosync.git
Open in Android Studio: Select File > Open and choose the project root directory.

Sync Gradle: Wait for dependencies to download.

Build & Run: Connect your Android device and click the "Run" button.

Permissions
The app requires the following permissions:

BLUETOOTH_SCAN & BLUETOOTH_CONNECT (Android 12+)

ACCESS_FINE_LOCATION (For BLE scanning on older Android versions)

POST_NOTIFICATIONS (For alerts)

FOREGROUND_SERVICE (To maintain connection)

🧪 Testing Without Hardware (Demo Mode)
If you do not have the custom HydroSync BLE hardware, you can use the built-in Demo Mode:

Launch the app and complete the Onboarding.

Navigate to Settings via the bottom navigation bar.

Tap the "Generate Demo Data (Dev)" button.

Wait for the toast message "24h of mock data added!".

Go to Dashboard or Trends to see the populated charts and logs.

🤝 Contributing
Fork the repository.

Create your feature branch (git checkout -b feature/AmazingFeature).

Commit your changes (git commit -m 'Add some AmazingFeature').

Push to the branch (git push origin feature/AmazingFeature).

Open a Pull Request.

📄 License
Distributed under the MIT License. See LICENSE for more information.
