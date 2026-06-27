package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.repository.WallpaperRepository

/** Loads categories exposed by Scapes API for global navigation tabs. */
class GetCategoriesUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(): ScapesResult<List<WallpaperCategory>> =
        wallpaperRepository.getCategories()
}
