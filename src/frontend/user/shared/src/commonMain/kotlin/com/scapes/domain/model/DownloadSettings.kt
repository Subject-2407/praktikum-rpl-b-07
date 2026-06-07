package com.scapes.domain.model

/**
 * User preference for wallpaper downloads.
 *
 * @property folderPath absolute destination folder path.
 * @property organiseBySource whether saved files are grouped by [WallpaperSource].
 */
data class DownloadSettings(
    val folderPath: String,
    val organiseBySource: Boolean = true,
)
