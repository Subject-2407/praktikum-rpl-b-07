# Scapes User App

Kotlin Multiplatform client for regular Scapes users. The project targets Android and
desktop JVM/Windows, with shared business logic in `shared`.

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
| `EncryptedStorage` | Android encrypted preferences and Desktop secure storage strategy. |
| `platformModule()` | Koin bindings for platform actual classes and `ScapesAppConfig` defaults. |

## Folder Guide

Use this guide when deciding where a change belongs.

| Path | Purpose | Put changes here when |
|---|---|---|
| `shared/src/commonMain/kotlin/com/scapes/domain/model` | Core app data types. | Adding/changing stable models like `Wallpaper`, `ApiKey`, `WallpaperSource`, `ApplyTarget`. |
| `shared/src/commonMain/kotlin/com/scapes/domain/repository` | Repository interfaces. | A feature needs a new data boundary that domain/use cases call. |
| `shared/src/commonMain/kotlin/com/scapes/domain/usecase` | Business actions. | You add validation or orchestration that is not UI-specific. |
| `shared/src/commonMain/kotlin/com/scapes/data/remote` | API clients, DTOs, API config. | Integrating Pexels, Unsplash, Pixabay, Scapes API, or changing network mapping. |
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

## Feature Branch Workflow

Use one branch family per use case:

```text
feature/frontend/<use-case>/integration
feature/frontend/<use-case>/shared
feature/frontend/<use-case>/mobile
feature/frontend/<use-case>/desktop
```

Example:

```text
feature/frontend/search-wallpapers/integration
feature/frontend/search-wallpapers/shared
feature/frontend/search-wallpapers/mobile
feature/frontend/search-wallpapers/desktop
```

Branch roles:

- `integration`: use-case integration branch. It should contain the stable combined state for
  that use case and become the base for review/merge.
- `shared`: common KMP work used by both platforms, especially `shared/src/commonMain`.
- `mobile`: Android-specific work, including `androidApp` and `shared/src/androidMain`.
- `desktop`: Desktop-specific work, including `desktopApp` and `shared/src/desktopMain`.

Recommended flow:

```bash
git switch -c feature/frontend/<use-case>/shared <base-branch>
# implement commonMain/domain/data/presentation changes
git commit -m "feat(frontend-user): add shared <use-case> flow"

git switch -c feature/frontend/<use-case>/integration

git switch -c feature/frontend/<use-case>/mobile feature/frontend/<use-case>/integration
# implement androidApp and androidMain changes
git commit -m "feat(frontend-user): wire android <use-case>"

git switch -c feature/frontend/<use-case>/desktop feature/frontend/<use-case>/integration
# implement desktopApp and desktopMain changes
git commit -m "feat(frontend-user): wire desktop <use-case>"
```

When shared changes are updated after platform branches already exist:

```bash
git switch feature/frontend/<use-case>/shared
# commit shared update

git switch feature/frontend/<use-case>/integration
git merge --ff-only feature/frontend/<use-case>/shared

git switch feature/frontend/<use-case>/mobile
git rebase feature/frontend/<use-case>/integration

git switch feature/frontend/<use-case>/desktop
git rebase feature/frontend/<use-case>/integration
```

Push with upstream tracking:

```bash
git push -u origin feature/frontend/<use-case>/integration
git push -u origin feature/frontend/<use-case>/shared
git push -u origin feature/frontend/<use-case>/mobile
git push -u origin feature/frontend/<use-case>/desktop
```

If a platform branch was rebased after it had already been pushed, use:

```bash
git push --force-with-lease
```

Do not create both `feature/frontend/<use-case>` and
`feature/frontend/<use-case>/mobile` on the same remote. Git treats the first name as a ref,
so it blocks nested branch names.

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
- Do not add raw exceptions to presentation state. Map failures to `ScapesResult.Error`.
