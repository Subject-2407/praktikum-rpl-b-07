package com.scapes.domain.usecase

import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.repository.WallpaperRepository

/** Applies a selected wallpaper through the platform boundary. */
class ApplyWallpaperUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(wallpaper: Wallpaper, target: ApplyTarget): ScapesResult<Unit> =
        wallpaperRepository.applyWallpaper(wallpaper, target)
}
