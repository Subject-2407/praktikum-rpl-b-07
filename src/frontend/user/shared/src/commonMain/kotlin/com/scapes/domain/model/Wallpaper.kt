package com.scapes.domain.model

/**
 * Wallpaper metadata used by the Scapes user app.
 *
 * @property id provider-stable wallpaper identifier.
 * @property title display name shown to users.
 * @property source wallpaper provider.
 * @property previewUrl image URL optimized for browsing.
 * @property fullImageUrl image URL used for download and apply flows.
 * @property authorName optional creator attribution.
 * @property localPath path after the image has been downloaded.
 */
data class Wallpaper(
    val id: String,
    val title: String,
    val source: WallpaperSource,
    val previewUrl: String,
    val fullImageUrl: String,
    val authorName: String? = null,
    val localPath: String? = null,
)
