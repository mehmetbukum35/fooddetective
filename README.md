# Food Label Detective

Food Label Detective is an Android app that helps users read food labels, detect E-codes, and review additive information such as purpose, halal status, risk level, and health notes.

The app works with a local Room database and can synchronize additive records from a remote API. It also supports OCR-based label scanning through the camera or gallery images.

## Features

- Search additives by E-code, such as `E330`, `E120`, or `E160A`.
- Detect E-codes from food label photos with on-device OCR.
- Scan labels with CameraX or choose an image from the gallery.
- Show additive purpose, halal status, health note, warning, and risk level.
- Work offline with a bundled SQLite database.
- Synchronize additive records from a remote API when available.
- Support Turkish and English UI strings.
- Support light and dark themes.
- Show animated day/night header visuals.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Retrofit
- OkHttp
- ML Kit Text Recognition
- CameraX
- Kotlin Coroutines
- JUnit

## Project Structure

```text
app/src/main/java/com/mehmetbukum/fooddetective
├── data/                  # Room entities, DAO, repository, sync result models
├── data/remote/           # Retrofit API layer
├── di/                    # Manual application dependency container
├── localization/          # Language preferences and locale provider
├── ocr/                   # Camera and ML Kit OCR helpers
├── ui/components/         # Reusable Compose UI components
├── ui/screens/            # Main app screens
└── ui/theme/              # Theme and color configuration
```

## Database and Sync

The app ships with a bundled SQLite asset:

```text
app/src/main/assets/database/e_katki_maddeleri_sade.sqlite
```

Room loads this asset on first install. The app can later synchronize with the remote additive API. Sync results are represented with typed sealed reasons instead of hardcoded UI strings in the data layer.

Current sync reason flow:

```text
AdditiveRepository -> SyncResult / SyncSkipReason / SyncErrorReason
FoodDetectiveViewModel -> SyncMessageMapper -> UiText.Resource
UI -> localized string resources
```

Explicit Room migrations are registered from version 1 to version 6. Destructive migration is not used.

## Manual Dependency Injection

The app uses a lightweight manual DI container instead of a full framework:

```text
FoodDetectiveApplication
└── AppContainer
    ├── AppDatabase
    ├── AdditiveRepository
    └── FoodDetectiveViewModel factory method
```

This keeps dependency creation outside `MainActivity` while avoiding extra framework complexity.

## Build

From the project root:

```bash
./gradlew clean
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat clean
gradlew.bat assembleDebug
```

## Run Unit Tests

```bash
./gradlew testDebugUnitTest
```

On Windows:

```bat
gradlew.bat testDebugUnitTest
```

## Release Build Notes

Release signing is intentionally validated in `app/build.gradle.kts`. To build a release APK or AAB, create a `keystore.properties` file in the project root with the required signing values:

```properties
storeFile=path/to/your-release-key.jks
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

Then run:

```bash
./gradlew assembleRelease
# or
./gradlew bundleRelease
```

Do not commit keystore files or real signing passwords.

## Privacy Notes

Label images captured with the camera or selected from the gallery are used for on-device OCR processing. The app does not need to upload label images for text recognition. Internet access is used for additive database synchronization and connection checks.

## Recommended Development Checks

Before pushing larger changes, run:

```bash
./gradlew clean
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The GitHub Actions workflow runs unit tests and debug build automatically on pushes and pull requests.

## Disclaimer

This app is for general information only. It does not replace product labels, manufacturer statements, official notices, certification bodies, medical guidance, or legal advice.
