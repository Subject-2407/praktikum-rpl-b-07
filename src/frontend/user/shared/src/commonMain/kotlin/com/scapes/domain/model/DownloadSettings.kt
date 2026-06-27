package com.scapes.domain.model

/**
 * User preference for wallpaper downloads.
 *
 * @property folderPath absolute destination folder path.
 * @property organization folder grouping strategy for saved wallpapers.
 */
data class DownloadSettings(
    val folderPath: String,
    val organization: DownloadOrganization = DownloadOrganization.BY_CATEGORY,
)

/** Folder grouping strategy for downloaded wallpapers. */
enum class DownloadOrganization {
    /** Store files under a folder named from wallpaper category, falling back to source. */
    BY_CATEGORY,

    /** Store files under a folder named from wallpaper source. */
    BY_SOURCE,

    /** Store every file directly inside [DownloadSettings.folderPath]. */
    NONE,
}
