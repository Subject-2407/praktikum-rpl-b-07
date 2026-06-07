package com.scapes.domain.repository

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource

/**
 * Wallpaper search, download, and apply boundary.
 */
interface WallpaperRepository {
    /**
     * Searches [source] for wallpapers matching [query].
     */
    suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
    ): ScapesResult<List<Wallpaper>>

    /**
     * Downloads [wallpaper] into the configured local folder.
     */
    suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper>

    /**
     * Applies [wallpaper] to the requested platform [target].
     */
    suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
    ): ScapesResult<Unit>
}
