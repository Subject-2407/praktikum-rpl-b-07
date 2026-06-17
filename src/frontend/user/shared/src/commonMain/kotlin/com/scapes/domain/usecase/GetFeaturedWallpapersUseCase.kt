package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.WallpaperRepository

/** Loads provider-curated featured wallpapers for the landing hero section. */
class GetFeaturedWallpapersUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(
        page: Int = 0,
        source: WallpaperSource,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
    ): ScapesResult<List<Wallpaper>> {
        if (page < 0) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Featured wallpaper page cannot be negative.",
            )
        }

        return wallpaperRepository.getFeaturedWallpapers(
            page = page,
            source = source,
            targetDevice = targetDevice,
        )
    }
}
