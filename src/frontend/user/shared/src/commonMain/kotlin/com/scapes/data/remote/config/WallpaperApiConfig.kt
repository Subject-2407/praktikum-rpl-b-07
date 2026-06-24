package com.scapes.data.remote.config

/**
 * Runtime configuration for public wallpaper providers.
 *
 * Platform shells can pass build-time values later; shared code keeps safe empty defaults.
 */
data class WallpaperApiConfig(
    val scapesBaseUrl: String = DefaultScapesBaseUrl,
    val pexelsApiKey: String = com.scapes.shared.BuildKonfig.PEXELS_API_KEY,
    val unsplashAccessKey: String = com.scapes.shared.BuildKonfig.UNSPLASH_ACCESS_KEY,
    val pixabayApiKey: String = com.scapes.shared.BuildKonfig.PIXABAY_API_KEY,
) {
    companion object {
        const val DefaultScapesBaseUrl = "https://scapes.my.id"
    }
}
