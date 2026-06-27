package com.scapes.domain.model

/** Stable error categories shared across domain, data, and presentation. */
enum class ErrorCode {
    /** User input does not satisfy validation rules. */
    VALIDATION,

    /** Remote provider or network call failed. */
    NETWORK,

    /** Provider rejected the supplied credentials. */
    UNAUTHORIZED,

    /** Requested wallpaper or metadata was not found. */
    NOT_FOUND,

    /** Local storage operation failed. */
    STORAGE,

    /** Operation is unavailable on the current platform. */
    UNSUPPORTED_PLATFORM,

    /** Fallback category for unmapped errors. */
    UNKNOWN,
}
