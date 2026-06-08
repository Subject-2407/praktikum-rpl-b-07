package com.scapes.data.repository

import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.WallpaperRepository

class ExternalWallpaperRepository(
    private val externalWallpaperApi: ExternalWallpaperApi,
) : WallpaperRepository {
    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
    ): ScapesResult<List<Wallpaper>> =
        externalWallpaperApi.searchWallpapers(
            query = query,
            page = page + 1,
            source = source,
        )

    override suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper> =
        ScapesResult.Error(
            code = ErrorCode.UNSUPPORTED_PLATFORM,
            message = "Saving wallpapers is not implemented yet.",
        )

    override suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
    ): ScapesResult<Unit> =
        ScapesResult.Error(
            code = ErrorCode.UNSUPPORTED_PLATFORM,
            message = "Applying wallpapers is not implemented yet.",
        )
}
