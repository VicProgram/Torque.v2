---
name: build-gradle
description: Use when modifying build configuration — Gradle tasks, version catalog, AGP, Kotlin/Compose compiler, Detekt, ProGuard, signing config, or CI/CD pipeline.
---

# Build & Gradle Conventions for Torque

## Key Build Parameters

- **Gradle**: 8.12.1
- **AGP**: 8.9.2
- **Kotlin**: 2.1.20
- **KSP**: 2.1.20-1.0.32
- **Min SDK**: 28 (Android 9) | **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35
- **Compose BOM**: 2025.05.01

## Module Structure

3 modules defined in `settings.gradle.kts`:
```
include(":app")
include(":core")
include(":data")
```

## Version Catalog

All dependency versions in `gradle/libs.versions.toml`:
```toml
[versions]
agp = "8.9.2"
kotlin = "2.1.20"
composeBom = "2025.05.01"
room = "2.6.1"
koin = "3.5.6"
```

Reference in build files as: `alias(libs.plugins.android.application)` or `libs.androidx.room.runtime`.

## Common Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Tests
./gradlew testDebugUnitTest
./gradlew connectedCheck

# Static analysis
./gradlew detekt

# Clean
./gradlew clean
```

## Detekt

Config file: `config/detekt/detekt.yml`
Disabled rules: `LongMethod`, `LongParameterList`, `LargeClass`, `MagicNumber` (and others).
Run: `./gradlew detekt`

## ProGuard

File: `app/proguard-rules.pro`
Rules for: Room entities, Compose, Coroutines, DataStore, Sentry, Koin.
Release builds use minification + shrinking + ProGuard.

## Signing

Release signing via `keystore.properties` file (not committed to git).

## CI/CD

GitHub Actions workflow at `.github/workflows/ci.yml`:
1. JDK 17 setup
2. Detekt analysis
3. Unit tests (`./gradlew test`)
4. Build debug APK (`./gradlew assembleDebug`)
5. Upload APK as artifact

## Sentry

- DSN passed via BuildConfig field `SENTRY_DSN`.
- Placeholder DSN by default — Sentry only initializes if DSN is not placeholder.
- Sentry auto-init disabled in manifest (initialized manually in `TorqueApp.kt`).
