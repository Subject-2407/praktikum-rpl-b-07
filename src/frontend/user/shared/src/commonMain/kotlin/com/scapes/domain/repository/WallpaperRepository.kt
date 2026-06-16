package com.scapes.domain.repository

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo

/** Wallpaper search, download, and apply boundary. */
interface WallpaperRepository {
    /** Loads user-selectable wallpaper sources. */
    suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>>

    /** Searches [source] for wallpapers matching [query]. */
    suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
    ): ScapesResult<List<Wallpaper>>

    /** Downloads [wallpaper] into the configured local folder. */
    suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper>

    /** Applies [wallpaper] to the requested platform [target]. */
    suspend fun applyWallpaper(wallpaper: Wallpaper, target: ApplyTarget): ScapesResult<Unit>

    /** Validates a personal API key against a third-party provider. */
    suspend fun validateApiKey(source: WallpaperSource, apiKey: String): ScapesResult<Unit>

    /** Clears any cached search data tied to [source]. */
    fun invalidateSource(source: WallpaperSource)
}
