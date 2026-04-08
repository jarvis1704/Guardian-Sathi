# Guardian Sathi - Android Project Overview

Guardian Sathi is a modern Android application focused on safety and companionship ("Sathi" means companion in Hindi/Bengali). The project leverages the latest Android development practices and libraries.

## 🛠 Tech Stack

- **Language:** Kotlin (100%)
- **UI Framework:** Jetpack Compose (Modern declarative UI)
- **Dependency Injection:** Hilt (Dagger-based DI for Android)
- **Networking:** Retrofit with Gson converter
- **Image Loading:** Coil
- **Asynchronous Programming:** Kotlin Coroutines
- **Navigation:** Jetpack Navigation Compose
- **Error/Bug Reporting:** BugSnap (Integrated for real-time bug capturing)
- **Architecture:** Modern Android Architecture (MVVM/MVI)

## 📁 Project Structure

The project follows a modular-ready structure:

- `app/`: Main application module.
  - `src/main/java/com/biprangshu/guardiansathi/Global/`: Global configurations, MainActivity, and base UI components (Theme, Type, etc.).
  - `src/main/java/com/biprangshu/guardiansathi/Guardian/`: Potentially feature-specific code for the "Guardian" persona/feature.
  - `src/main/res/`: Android resources (drawables, values, XML configs).

## 🚀 Building and Running

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 17+ (Project uses Java 11 compatibility but build-logic might require higher).
- Android SDK 36 (Compile SDK).

### Commands
- **Build Debug APK:** `./gradlew assembleDebug`
- **Install on Device:** `./gradlew installDebug`
- **Run Unit Tests:** `./gradlew test`
- **Run Instrumented Tests:** `./gradlew connectedAndroidTest`
- **Lint Check:** `./gradlew lint`

## 📝 Development Conventions

- **UI:** All new UI should be built using **Jetpack Compose**. Avoid XML layouts unless strictly necessary for legacy integration.
- **Dependency Injection:** Use **Hilt** for providing dependencies. Every `ViewModel` should be annotated with `@HiltViewModel`.
- **State Management:** Prefer `mutableStateOf` or `Flow` (StateFlow/SharedFlow) for managing UI state.
- **Version Management:** All dependency versions are managed in `gradle/libs.versions.toml`. Do not hardcode versions in `build.gradle.kts`.
- **Error Handling:** `BugSnap` is initialized in `MainActivity`. Ensure it's active during development to capture crashes and UI issues.

## 🌐 Localization

The app currently supports:
- English
- Hindi (हिन्दी)
- Bengali (বাংলা)
- Tamil (தமிழ்)

Strings are managed in `res/values/strings.xml` and localized variants (e.g., `res/values-hi/strings.xml`).
