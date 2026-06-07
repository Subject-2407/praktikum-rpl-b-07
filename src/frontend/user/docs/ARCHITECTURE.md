# Architecture

Scapes User App follows Clean Architecture with Kotlin Multiplatform source sets.

## Dependency Direction

`presentation -> domain <- data -> platform`

Rules:

- `domain` contains models, repository interfaces, and use cases.
- `domain` must not import `data`, `presentation`, or `platform`.
- `data` implements repository contracts and maps DTOs to domain models.
- `presentation` depends on use cases and domain models, not data implementations.
- `platform` exposes OS-specific capabilities through `expect`/`actual`.

## Initial Packages

- `com.scapes.domain.model`
- `com.scapes.domain.repository`
- `com.scapes.domain.usecase`
- `com.scapes.di`
- `com.scapes.platform`
- `com.scapes.presentation.ui`

## Placeholder Policy

Platform code that needs OS context or secure APIs currently fails safely with
`UnsupportedOperationException`. Replace those placeholders only when wiring the real Android
or Windows implementation.

Do not replace secure-storage placeholders with plaintext storage.
