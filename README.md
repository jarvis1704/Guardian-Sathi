<div align="center">

# Guardian Saathi
### *साथी — your elder's silent guardian*

A dual-persona Android safety app connecting elderly users with trusted family members through real-time monitoring, emergency response, and AI-powered assistance.

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://android.com)
[![Min SDK](https://img.shields.io/badge/Min_SDK-26_(Oreo)-blue)](https://developer.android.com/about/versions/oreo)
[![Target SDK](https://img.shields.io/badge/Target_SDK-36-blue)](https://developer.android.com/about/versions/15)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-2024.12-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-33.7.0-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)

</div>

---

## About

Guardian Saathi pairs two roles on a single platform: the **Elder** (person being watched over) and the **Guardian** (trusted family member doing the watching). The Elder's device runs a persistent foreground service that streams live location, battery status, and fall events to Firebase — all surfaced in the Guardian's real-time dashboard.

Beyond monitoring, the app arms the Elder with a panic SOS button, AI voice assistant (Gemini), medicine reminders, and intelligent scam/fraud SMS detection — making it a full safety companion, not just a tracker.

---

## Screenshots

> **Note:** Replace the placeholder paths below with actual screenshots from your device.

### Elder Persona

| Home | Fall Alert | Panic SOS | Voice Assistant |
|:---:|:---:|:---:|:---:|
| <!-- add screenshot: Elder home screen --> <br>*Elder Home* | <!-- add screenshot: Fall detection alert --> <br>*Fall Alert* | <!-- add screenshot: SOS panic button --> <br>*Panic SOS* | <!-- add screenshot: Voice assistant --> <br>*Voice Assistant* |

### Guardian Persona

| Dashboard | Location Map | Alerts | Profile |
|:---:|:---:|:---:|:---:|
| <!-- add screenshot: Guardian dashboard --> <br>*Dashboard* | <!-- add screenshot: Live location map --> <br>*Location Map* | <!-- add screenshot: Alert history --> <br>*Alerts* | <!-- add screenshot: Guardian profile --> <br>*Profile* |

---

## Features

### Elder Persona
| Feature | Description |
|---|---|
| **Fall Detection** | Accelerometer-based 3-phase detection (free fall → impact → stillness). Triggers full-screen alarm and Guardian alert. |
| **Panic SOS** | One-tap emergency button. Fires push notification to linked Guardian instantly. |
| **Medicine Reminders** | Schedule medications with take/skip actions. Powered by `AlarmManager` + `BroadcastReceiver`. |
| **Voice Assistant** | Natural language assistant using Google Gemini API. Hands-free for accessibility. |
| **Scam / Fraud Detection** | AI-powered SMS scanning to flag phishing, fraud, and unknown callers. |
| **Emergency Contacts** | Locally stored contacts (Room DB) for quick SOS access without internet. |
| **QR Link to Guardian** | Generates a QR code for secure one-time pairing with a Guardian account. |
| **Multi-language UI** | Full interface in English, Hindi, Bengali, and Tamil. |

### Guardian Persona
| Feature | Description |
|---|---|
| **Live Location Map** | Real-time map of Elder's location, updated every 3 minutes via Firebase RTDB. |
| **Battery Monitoring** | Live battery level and charging status, updated every 5 minutes. |
| **Push Alerts** | FCM notifications for falls, SOS presses, and critical events. |
| **Alert History** | Timestamped log of all emergency events. |
| **QR Code Scanning** | Scan Elder's QR code to complete secure linking. |
| **Guardian Profile** | Manage account, view linked Elder details, logout. |

---

## Tech Stack

### Core
| Category | Library / Tool | Version |
|---|---|---|
| Language | Kotlin | 2.1.0 |
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| Architecture | MVVM / MVI | — |
| Navigation | Compose Navigation (type-safe) | — |
| DI | Hilt | 2.59.2 |
| Coroutines | Kotlin Coroutines | 1.10.2 |

### Firebase
| Service | Use |
|---|---|
| Authentication | Google Sign-In |
| Cloud Firestore | User profiles, link codes, link status |
| Realtime Database | Live Elder sensor data (location, battery) |
| Cloud Messaging (FCM) | Push notifications for alerts |

### AI / ML
| Library | Use |
|---|---|
| Google Gemini API | Voice assistant natural language processing |
| ML Kit Barcode Scanning | QR code detection for Guardian-Elder linking |
| ZXing | QR code generation on Elder side |

### Storage & Networking
| Library | Use | Version |
|---|---|---|
| Room | Local emergency contacts DB | 2.8.4 |
| DataStore | Session state (`UserSessionManager`) | 1.1.3 |
| Retrofit | REST networking | 3.0.0 |
| Google Maps Compose | Live location map | 6.1.2 |
| Google Places API | Location search | 5.2.0 |

### UI Utilities
| Library | Use |
|---|---|
| Coil | Image loading |
| Lottie | Animations |
| CameraX | Camera for QR scanning |
| Accompanist Permissions | Runtime permission handling |
| WorkManager | Background task scheduling |

---

## Architecture

```
app/
├── Global/              # Shared across both personas
│   ├── core/            # Utilities, FCMService
│   ├── data/            # Firebase auth/Firestore sources, UserSessionManager (DataStore)
│   ├── di/              # Firebase, auth, session Hilt modules
│   ├── domain/          # AuthRepository, Result<D,E>, DataError
│   └── presentation/    # AppNav (nav host), login, registration, onboarding, splash, theme
│
├── Elder/               # Elder persona
│   ├── core/            # GuardianService (foreground), FallDetector, location/battery helpers
│   │                    # GuardianNotificationListenerService
│   ├── data/            # Firebase & location repo impls, Room (GuardianContact)
│   ├── di/              # Room, maps, permissions, voice assistant Hilt modules
│   └── presentation/    # ElderHomeScreen, PanicSOSPage, VoiceAssistantPage,
│                        # FallAlarmScreen, EmergencyContactsPage, ElderSettingsPage
│
└── Guardian/            # Guardian persona
    ├── core/            # QR code scanning (QrCodeAnalyser, scanQrFromUri)
    ├── Navigation/      # GuardianShell (bottom-nav Scaffold), GuardianTabs
    └── presentation/    # GuardianHomeRoot, GuardianReminderPage,
                         # GuardianAlertPage, GuardianProfileRoot
```

### Navigation Graphs

| Graph | Start | Purpose |
|---|---|---|
| `RegistrationGraph` | `SplashRoute` | Language → Onboarding → Login → Registration |
| `ElderGraph` | `LinkGuardianRoute` | Elder link flow → Elder home & sub-screens |
| `GuardianGraph` | `LinkElderRoute` | Guardian link flow → `GuardianShellRoute` (bottom-nav) |

`SplashViewModel` reads `UserSessionManager` on cold start to decide which graph to enter.

### Key Design Patterns

- **`Result<D, E>`** — All repository methods return a sealed `Result` type. Helpers: `map`, `onSuccess`, `onFailure`, `asEmptyResult`.
- **`UserSessionManager`** — DataStore singleton tracking login state, role, link status, and persona display names/photos.
- **Foreground Service** — `GuardianService` runs continuously on Elder device (START_STICKY, restarts on boot via `BootReceiver`). Streams location every 3 min and battery every 5 min to Firebase RTDB.
- **Firebase split** — Realtime Database for high-frequency live sensor data; Firestore for structured user and link data.
- **One-shot event channel** — ViewModels expose `Channel<Event>` for navigation and one-time UI events (e.g., logout, permission prompts).

---

## Prerequisites

- **Android Studio** Meerkat (2024.3) or newer
- **JDK 11+**
- **Android device or emulator** running API 26+
- **Firebase project** with Authentication, Firestore, Realtime Database, and Cloud Messaging enabled
- **Google Maps API key** (Android restrictions)
- **Google Gemini API key**

---

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/[your-repo-url]/Guardian-Sathi.git
cd Guardian-Sathi
```

### 2. Add Firebase configuration

Download `google-services.json` from your Firebase project console and place it at:

```
app/google-services.json
```

### 3. Configure API keys

Create `local.properties` in the project root (if it does not exist) and add:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
GEMINI_API_KEY=your_gemini_api_key_here
```

> Both keys are injected at build time via `BuildConfig` fields and `manifestPlaceholders`. Never commit `local.properties` to version control.

### 4. Sync and build

Open the project in Android Studio, let Gradle sync, then run on a physical device or emulator.

---

## Build Commands

| Command | Description |
|---|---|
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew installDebug` | Build and install on connected device |
| `./gradlew test` | Run unit tests |
| `./gradlew connectedAndroidTest` | Run instrumented tests (requires device/emulator) |
| `./gradlew lint` | Run lint checks |
| `./gradlew :app:kspDebugKotlin` | Run KSP code generation (Room, Hilt) |

---

## Localization

The app is fully localized in four languages. All user-visible strings must be added to each locale:

| Language | Resource directory |
|---|---|
| English | `res/values/strings.xml` |
| Hindi | `res/values-hi/strings.xml` |
| Bengali | `res/values-bn/strings.xml` |
| Tamil | `res/values-ta/strings.xml` |

---

## Required Permissions

| Permission | Reason |
|---|---|
| `ACCESS_FINE_LOCATION`, `ACCESS_BACKGROUND_LOCATION` | Live Elder location tracking |
| `ACTIVITY_RECOGNITION` | Fall detection via accelerometer |
| `CAMERA` | QR code scanning for Guardian-Elder link |
| `RECORD_AUDIO` | Voice assistant input |
| `READ_SMS`, `RECEIVE_SMS` | Scam/fraud SMS detection |
| `USE_FULL_SCREEN_INTENT` | Fall alarm screen over lock screen |
| `RECEIVE_BOOT_COMPLETED` | Restart foreground service after reboot |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keep foreground service alive |
| `SCHEDULE_EXACT_ALARM` | Medicine reminder precision timing |

---

<div align="center">

Built with care for families who want to stay close, no matter the distance.

</div>
