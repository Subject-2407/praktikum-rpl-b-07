package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.repository.WallpaperRepository

/** Downloads and stores a wallpaper in the configured folder. */
class SaveWallpaperUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(wallpaper: Wallpaper): ScapesResult<Wallpaper> =
        wallpaperRepository.saveWallpaper(wallpaper)
}
