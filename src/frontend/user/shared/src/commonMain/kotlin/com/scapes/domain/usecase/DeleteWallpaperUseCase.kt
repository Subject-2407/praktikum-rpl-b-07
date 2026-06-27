package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.repository.WallpaperRepository

/** Deletes a wallpaper from the local file system and store. */
class DeleteWallpaperUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(wallpaper: Wallpaper): ScapesResult<Unit> =
        wallpaperRepository.deleteWallpaper(wallpaper)
}
