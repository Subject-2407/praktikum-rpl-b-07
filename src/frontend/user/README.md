# Scapes User App

Kotlin Multiplatform client for regular Scapes users. The project targets Android and
desktop JVM/Windows, with shared business logic in `shared`.

## Modules

- `shared`: domain, data boundaries, presentation UI, platform abstractions, SQLDelight schema.
- `androidApp`: Android application shell and manifest-level platform setup.
- `desktopApp`: Compose Desktop application shell and native distribution config.

## Local Setup

Use JDK 17 and Android SDK with API 36 installed.

Create a local secret file from the example:

```bash
cp local.properties.example local.properties
```

Fill only local placeholders. Do not commit `local.properties`.

This environment does not have Gradle installed, so the wrapper was not generated here. On a
machine with Gradle available, generate it from this directory:

```bash
gradle wrapper --gradle-version 8.14.3
```

After the wrapper exists, prefer the wrapper commands below.

## Useful Commands

```bash
./gradlew build
./gradlew check
./gradlew detekt
./gradlew spotlessCheck
./gradlew spotlessApply
./gradlew allTests
./gradlew :androidApp:assembleDebug
./gradlew :desktopApp:run
./gradlew :desktopApp:packageDistributionForCurrentOS
```

## Development Rules

- User role only: do not add contributor, moderation, or admin features here.
- Keep business rules in `shared/src/commonMain/kotlin/com/scapes/domain`.
- Keep platform OS calls behind `expect`/`actual` classes in `shared/src/*Main`.
- Do not store API keys outside `EncryptedStorage`.
- Do not add raw exceptions to presentation state. Map failures to `ScapesResult.Error`.
