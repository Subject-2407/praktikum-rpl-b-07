package com.scapes.domain.model

/** User-selectable wallpaper source metadata. */
data class WallpaperSourceInfo(
    val source: WallpaperSource,
    val label: String,
    val baseUrl: String,
    val isDefault: Boolean = false,
    val requiresPersonalApiKey: Boolean = false,
)
