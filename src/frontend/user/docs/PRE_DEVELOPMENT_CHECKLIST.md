# Pre-development Checklist

Use this checklist before feature work starts.

## Build Foundation

- [x] Gradle KMP module structure is present.
- [x] Version catalog is the single source of dependency versions.
- [x] Android, desktop, and shared modules are declared.
- [x] JDK target is pinned to 17.
- [x] Android minimum SDK is pinned to API 26.
- [ ] Gradle wrapper is generated on a machine with Gradle installed.

## Quality Gates

- [x] `.editorconfig` enforces UTF-8, LF, 4-space Kotlin indentation, and 100 columns.
- [x] Spotless with ktfmt is wired for Kotlin and Gradle Kotlin scripts.
- [x] Detekt is wired with baseline architecture-friendly rules.
- [x] Kover plugin is present for module coverage reporting.
- [ ] Kover verification thresholds are finalized once real packages have tests.
- [ ] CI workflow is added at repository root after the team confirms pipeline location.

## Architecture

- [x] Domain models are initialized in `commonMain`.
- [x] Repository contracts are initialized in `domain/repository`.
- [x] Use case layer is initialized with search validation.
- [x] Platform seams are declared with `expect`/`actual`.
- [x] Android and desktop app shells are present.
- [ ] Data repository implementations are added when provider APIs are implemented.
- [ ] ViewModels are added when real screens are implemented.

## Security

- [x] `local.properties.example` documents local secret keys.
- [x] `local.properties` is ignored.
- [x] HTTP client masks `Authorization` headers in logs.
- [x] Android cleartext traffic is disabled.
- [ ] Android network certificate pins are added when the real API certificate is available.
- [ ] Desktop Scapes API certificate pinning is added when the real API certificate is available.
- [ ] Android FCM setup uses the main `firebase-messaging` module, not deprecated KTX modules.
- [ ] Android encrypted storage is wired with `EncryptedSharedPreferences`.
- [ ] Desktop encrypted storage is wired with Windows DPAPI.

## Testing

- [x] `commonTest` is present.
- [x] First validation tests cover search query normalization.
- [ ] Use case tests cover happy path, repository error, and edge cases.
- [ ] Repository tests use fakes or Ktor `MockEngine`.
- [ ] ViewModel tests use Turbine for `uiState` transitions.
- [ ] UI tests are added for core flows after screens exist.
