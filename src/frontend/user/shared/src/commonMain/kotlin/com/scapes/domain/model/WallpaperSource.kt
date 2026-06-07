package com.scapes.domain.model

/**
 * Search provider supported by Scapes.
 */
enum class WallpaperSource {
    /** First-party Scapes API. */
    SCAPES_API,

    /** Pexels public image API. */
    PEXELS,

    /** Unsplash public image API. */
    UNSPLASH,

    /** Pixabay public image API. */
    PIXABAY,
}
