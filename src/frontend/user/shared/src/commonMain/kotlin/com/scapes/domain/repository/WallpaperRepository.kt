package com.scapes.domain.repository

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.TrendingCategory
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo

/** Wallpaper search, download, and apply boundary. */
interface WallpaperRepository {
    /** Loads user-selectable wallpaper sources. */
    suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>>

    /** Loads Scapes category metadata used by global category tabs. */
    suspend fun getCategories(): ScapesResult<List<WallpaperCategory>>

    /** Loads trending categories used by the landing feed. */
    suspend fun getTrendingCategories(
        source: WallpaperSource,
        limit: Int = 10,
    ): ScapesResult<List<TrendingCategory>>

    /** Loads live search suggestions for the active [source]. */
    suspend fun getSearchRecommendations(
        query: String,
        source: WallpaperSource,
        limit: Int = 10,
    ): ScapesResult<List<SearchRecommendation>>

    /** Loads provider-curated featured wallpapers for landing discovery. */
    suspend fun getFeaturedWallpapers(
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
    ): ScapesResult<List<Wallpaper>>

    /** Loads wallpapers that have already been saved locally by the user. */
    suspend fun getDownloadedWallpapers(): ScapesResult<List<Wallpaper>>

    /** Searches [source] for wallpapers matching [query]. */
    suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
        categorySlug: String? = null,
        limit: Int? = null,
    ): ScapesResult<List<Wallpaper>>

    /** Downloads [wallpaper] into the configured local folder. */
    suspend fun saveWallpaper(wallpaper: Wallpaper, onProgress: suspend (Float) -> Unit = {}): ScapesResult<Wallpaper>

    /** Deletes [wallpaper] from the local store and filesystem. */
    suspend fun deleteWallpaper(wallpaper: Wallpaper): ScapesResult<Unit>

    /** Applies [wallpaper] to the requested platform [target]. */
    suspend fun applyWallpaper(wallpaper: Wallpaper, target: ApplyTarget, onProgress: suspend (Float) -> Unit = {}): ScapesResult<Unit>

    /** Validates a personal API key against a third-party provider. */
    suspend fun validateApiKey(source: WallpaperSource, apiKey: String): ScapesResult<Unit>

    /** Sends a best-effort search analytics event. */
    suspend fun logSearchEvent(
        query: String,
        source: WallpaperSource,
        resultCount: Int? = null,
    ): ScapesResult<Unit>

    /** Clears any cached search data tied to [source]. */
    fun invalidateSource(source: WallpaperSource)
}
