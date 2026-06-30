# scapes-app

Kotlin Multiplatform client for [Scapes](https://scapes.my.id) — browse, download, and apply wallpapers on Windows and Android. Shared business logic lives in `shared`; platform shells are in `androidApp` and `desktopApp`.

## Modules

- `shared`: domain, data boundaries, presentation UI, platform abstractions, SQLDelight schema.
- `androidApp`: Android application shell and manifest-level platform setup.
- `desktopApp`: Compose Desktop application shell and native distribution config.

## KMP Architecture Overview

This app uses Kotlin Multiplatform so Android and Desktop can share the same feature logic
while still keeping OS-specific details separate.

Dependency direction:

```text
presentation -> domain <- data -> platform
```

Meaning:

- `domain` defines what the app can do. It must stay pure Kotlin and must not know Compose,
  Ktor, SQLDelight, Android, or Desktop APIs.
- `data` implements repository contracts, calls remote APIs, reads/writes local data, and maps
  external DTOs into domain models.
- `presentation` renders UI and user interaction. It should depend on domain contracts and use
  cases, not on platform APIs directly.
- `platform` contains OS capabilities behind `expect`/`actual`, such as secure storage, file
  access, wallpaper applying, and back handling.

## Shared Feature Boundaries

The shared presentation layer is split by responsibility so platform teams can extend Android
and Desktop mechanics without touching unrelated feature state:

| Shared class | Responsibility |
|---|---|
| `ScapesViewModel` | App shell state: navigation, drawer, selected source, search text, theme. |
| `HomeViewModel` | Home discovery feed and source-driven landing sections. |
| `SearchViewModel` | Search results, pagination, save/apply action state. |
| `SettingsViewModel` | Download preferences and personal provider API key forms. |

Platform-specific implementation should plug into the shared layer through:

| Boundary | Implement here |
|---|---|
| `WallpaperApplier` | Android wallpaper manager and Windows/Desktop wallpaper APIs. |
| `FileSystemProvider` | Platform download folder, file writes, file listing, delete, write access checks. |
| `EncryptedStorage` | Secure API key storage only. Do not use it for settings or wallpaper metadata. |
| `PreferencesStorage` | Non-sensitive app preferences such as selected source and download settings. |
| `ScapesDatabaseFactory` | Platform SQLDelight database creation for local metadata stores. |
| `platformModule()` | Koin bindings for platform actual classes and `ScapesAppConfig` defaults. |

## Local Storage Architecture

Storage is intentionally split by data sensitivity and lifecycle:

| Data | Storage boundary | Backing store |
|---|---|---|
| Personal provider API keys | `EncryptedStorage` | Android encrypted preferences or Desktop DPAPI-backed storage. |
| App settings | `PreferencesStorage` | Platform key-value preferences. No encryption required. |
| Downloaded wallpaper metadata for Collections | `DownloadedWallpaperStore` | SQLDelight table `downloaded_wallpaper`. |
| Wallpaper image files | `FileSystemProvider` | User-selected platform folder or platform media storage. |

Download ownership should stay separated:

- `ExternalWallpaperApi` downloads image bytes through the shared Ktor client.
- `FileSystemProvider.saveFile(...)` writes those bytes to platform storage and returns the final local path.
- `ExternalWallpaperRepository` records Collection metadata only after the file write succeeds.

On Android, avoid making `FileSystemProvider.saveFile(...)` download the same URL again. If Android chooses
`DownloadManager`, wire it as an Android-specific download flow and update Collection metadata after the
download completion event, not immediately after `enqueue(...)`.

## Folder Guide

Use this guide when deciding where a change belongs.

| Path | Purpose | Put changes here when |
|---|---|---|
| `shared/src/commonMain/kotlin/com/scapes/domain/model` | Core app data types. | Adding/changing stable models like `Wallpaper`, `ApiKey`, `WallpaperSource`, `ApplyTarget`. |
| `shared/src/commonMain/kotlin/com/scapes/domain/repository` | Repository interfaces. | A feature needs a new data boundary that domain/use cases call. |
| `shared/src/commonMain/kotlin/com/scapes/domain/usecase` | Business actions. | You add validation or orchestration that is not UI-specific. |
| `shared/src/commonMain/kotlin/com/scapes/data/remote` | API clients, DTOs, API config. | Integrating Pexels, Unsplash, Pixabay, Scapes API, or changing network mapping. |
| `shared/src/commonMain/kotlin/com/scapes/data/local` | Local data stores backed by SQLDelight or platform storage abstractions. | Adding or changing persisted metadata such as downloaded wallpapers. |
| `shared/src/commonMain/kotlin/com/scapes/data/repository` | Repository implementations. | Combining API/local/platform dependencies behind a domain repository interface. |
| `shared/src/commonMain/kotlin/com/scapes/presentation` | Shared Compose UI and state. | UI is intended to work on Android and Desktop. |
| `shared/src/commonMain/kotlin/com/scapes/platform` | `expect` declarations. | Common code needs an OS capability without knowing Android/Desktop APIs. |
| `shared/src/androidMain/kotlin` | Android `actual` implementations. | Implementing Android-only behavior for an `expect`, such as `EncryptedStorage` or `BackHandler`. |
| `shared/src/desktopMain/kotlin` | Desktop `actual` implementations. | Implementing Desktop-only behavior for an `expect`, such as Windows APIs or Desktop no-op handlers. |
| `shared/src/commonMain/composeResources` | Shared UI assets. | Images/icons are used by shared Compose screens on more than one platform. |
| `shared/src/commonMain/sqldelight` | Shared local database schema. | Adding local metadata tables or queries used by repositories. |
| `shared/src/commonTest` | Common unit tests. | Testing domain, repository mapping, cache, validation, or use cases without platform APIs. |
| `androidApp` | Android app shell. | Changing manifest, launcher resources, activity/app startup, Android theme, permissions. |
| `desktopApp` | Desktop app shell. | Changing Desktop window entrypoint, JVM packaging, native distribution config. |

## What To Do In Each Layer

- In `domain`, keep logic framework-free. Use plain Kotlin, small models, repository interfaces,
  and `ScapesResult` for outcomes.
- In `data`, handle provider quirks, cache invalidation, DTO parsing, HTTP status mapping, and
  conversion into domain models. Do not leak DTOs into UI.
- In `presentation`, keep screens user-facing and state-driven. Avoid direct Ktor, SQLDelight,
  Android, or Desktop imports.
- In `platform`, only expose minimal capabilities through `expect`/`actual`. If you add an
  `expect`, add both Android and Desktop `actual` files in the same change, even if one side is
  a safe no-op or explicit unsupported implementation.
- In `androidApp` and `desktopApp`, keep only bootstrapping and platform shell concerns. Feature
  behavior should normally live in `shared`.

## Local Setup

Use JDK 17 and Android SDK with API 36 installed.

Create a local secret file from the example:

```bash
cp local.properties.example local.properties
```

Fill only local placeholders. Do not commit `local.properties`.

This environment does not have Gradle installed, so the wrapper was not generated here. On a
machine with Gradle available, generate it from this directory. Use Gradle 8.13 to match
Android Gradle Plugin 8.11.0:

```bash
gradle wrapper --gradle-version 8.13
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
- Do not store app settings or Collection metadata in `EncryptedStorage`.
- Keep downloaded wallpaper metadata in SQLDelight through `DownloadedWallpaperStore`.
- Keep image file writes behind `FileSystemProvider`; remote download remains owned by the shared Ktor API layer unless a platform branch deliberately introduces a platform-specific download flow.
- Do not add raw exceptions to presentation state. Map failures to `ScapesResult.Error`.
