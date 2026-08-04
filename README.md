# 🌙 Night Screen — Android Screen Dimmer

[![Release](https://img.shields.io/github/v/release/soupashh-ship-it/night-screen?style=flat-square&color=6C5CE7)](https://github.com/soupashh-ship-it/night-screen/releases)
[![Min API](https://img.shields.io/badge/API-26%2B-blue?style=flat-square)](https://developer.android.com/about/versions/oreo)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-purple?style=flat-square)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-M3-brightgreen?style=flat-square)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat-square)](LICENSE)
[![Offline](https://img.shields.io/badge/Privacy-100%25_Offline-success?style=flat-square)](#privacy--permissions)

A production-ready, ultra-lightweight, native Android **Night Screen / Screen Dimmer** application designed for comfortable night reading, OLED battery preservation, and eye protection.

Built with **Kotlin**, **Jetpack Compose**, **Material Design 3**, and native Android system overlay APIs.

---

## ⚡ Highlights & Features

- 🌙 **Deep Screen Dimming**: Reduces display intensity below your device's hardware minimum brightness.
- 🎨 **Eye-Care Presets & Custom RGB**:
  - **Neutral Black**: Maximum dark mode for OLED screen protection.
  - **Warm Amber**: Reduces blue-light strain during night use.
  - **Soft Orange & Deep Red**: Circadian-rhythm friendly tints for bed time.
  - **Custom Color Picker**: Fine-tune custom RGB hex shades and save custom presets.
- ⏱️ **Schedule Automation**: Automatically turn the dimmer filter on and off at scheduled times with day-of-week recurrence.
- ⚡ **Quick Settings (QS) Tile**: Toggle dimming instantly from Android's status shade without opening the app.
- 🔔 **Interactive Notification Bar**: Pause/Resume, Stop, or bump intensity (+10% / -10%) straight from your lock screen or status bar.
- 🛡️ **Touch Pass-through & Safety**: Uses `TYPE_APPLICATION_OVERLAY` with `FLAG_NOT_TOUCHABLE` & `FLAG_NOT_FOCUSABLE` — all taps pass through cleanly to apps underneath.
- 📐 **Dynamic Display Cutout Support**: Full-bleed coverage across display notches, hole punches, and system bars (`LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS`).
- 🔒 **100% Offline & Private**: Zero internet permission (`INTERNET` is omitted from manifest), zero tracking, zero analytics, zero ads.

---

## 📲 Download & Installation

### Option 1: Direct APK Download
Grab the latest release compiled and ready to install:
👉 **[Download Latest Release (APK)](https://github.com/soupashh-ship-it/night-screen/releases/latest)**

1. Download `NightScreen-v1.0.0.apk`.
2. Tap the APK file to install on your Android device (minSdk Android 8.0+ / API 26).
3. Open **Night Screen**, grant the **Display over other apps** (Overlay) permission, and tap the moon icon to activate.

---

## ⚙️ Architecture & Technical Stack

```
com.example.nightscreen
├── data
│   ├── model          # DimmerPreferences, FilterPreset, ScheduleConfig
│   └── repository     # UserPreferencesRepository (DataStore single source of truth)
├── notification       # NotificationFactory & NotificationActionReceiver
├── overlay            # OverlayController & TouchSafetyController (Opacity calculation)
├── scheduling         # ScheduleCoordinator, ScheduleCalculator, ScheduleAlarmReceiver
├── service            # OverlayService (Foreground Service with specialUse type)
├── tile               # DimmerTileService (Quick Settings Tile integration)
└── ui
    ├── components     # Material 3 design system tokens & reusable UI components
    ├── navigation     # Single-activity Compose Bottom Navigation
    ├── screens        # Main, Presets, Schedule, Settings, About screens
    └── viewmodel      # MainViewModel, ScheduleViewModel, PresetsViewModel
```

- **Language**: 100% Kotlin with Coroutines & StateFlow
- **UI Framework**: Jetpack Compose with Material 3 (Compose BOM 2024.08.00)
- **Persistence**: Jetpack DataStore Preferences (Non-blocking async disk I/O)
- **Background Engine**: Android Foreground Service (`specialUse` FGS type) with Android 14 API 34 compliance
- **Safety**: `TouchSafetyController` maps intensity to `InputManager.maximumObscuringOpacityForTouch`

---

## 🛠️ Building From Source

### Prerequisites
- Android Studio Jellyfish (2024.1.1) or newer
- JDK 17 / JDK 21
- Android SDK 34 (`minSdk 26`, `targetSdk 34`)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/soupashh-ship-it/night-screen.git
cd night-screen

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew testDebugUnitTest

# Run Lint Checks
./gradlew lint
```

The compiled APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Privacy & Permissions

Night Screen requires only minimal, necessary system permissions:

| Permission | Purpose |
| :--- | :--- |
| `SYSTEM_ALERT_WINDOW` | Required to display the screen dimmer overlay over other applications. |
| `FOREGROUND_SERVICE` / `SPECIAL_USE` | Keeps the dimming service reliable when the app is minimized. |
| `POST_NOTIFICATIONS` | Displays the interactive notification controls (Android 13+). |
| `RECEIVE_BOOT_COMPLETED` | Reschedules user-configured dimming alarms after device reboot. |

**NO INTERNET PERMISSION**: The app cannot connect to the internet. Your settings stay on your device forever.

---

## 📄 License

```text
Copyright 2026 soupashh-ship-it

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
