package com.scapes.data.remote.config

/**
 * Build-time API keys injected by each platform target.
 */
expect object ExternalWallpaperApiKeys {
    val pexelsApiKey: String
    val unsplashAccessKey: String
    val pixabayApiKey: String
}
