package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.repository.WallpaperRepository

/** Loads wallpapers that have already been saved on this device. */
class GetDownloadedWallpapersUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(): ScapesResult<List<Wallpaper>> =
        wallpaperRepository.getDownloadedWallpapers()
}
