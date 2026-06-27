package com.scapes.domain.model

/**
 * User-owned API key for a wallpaper provider.
 *
 * @property source provider that owns the key.
 * @property value raw key value, only handled inside secure storage boundaries.
 */
data class ApiKey(val source: WallpaperSource, val value: String)
