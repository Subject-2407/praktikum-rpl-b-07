package com.scapes.domain.model

/** Optional filters supported by Scapes API and represented in shared UI. */
data class SearchOptions(
    val query: String,
    val page: Int = 0,
    val source: WallpaperSource? = null,
    val categorySlug: String? = null,
    val tagSlugs: List<String> = emptyList(),
    val targetDevice: TargetDevice = TargetDevice.DESKTOP,
)
