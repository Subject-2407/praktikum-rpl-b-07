package com.scapes.data.remote.config

/**
 * Runtime configuration for public wallpaper providers.
 *
 * Platform shells can pass build-time values later; shared code keeps safe empty defaults.
 */
data class WallpaperApiConfig(
    val scapesBaseUrl: String = DefaultScapesBaseUrl,
    val pexelsApiKey: String = "",
    val unsplashAccessKey: String = "",
    val pixabayApiKey: String = "",
) {
    companion object {
        const val DefaultScapesBaseUrl = "https://scapes.my.id"
    }
}
