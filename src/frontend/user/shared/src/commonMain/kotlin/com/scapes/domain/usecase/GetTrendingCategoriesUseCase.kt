package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TrendingCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.WallpaperRepository

/** Loads landing categories sourced from backend search analytics. */
class GetTrendingCategoriesUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(
        source: WallpaperSource,
        limit: Int = 10,
    ): ScapesResult<List<TrendingCategory>> {
        if (limit !in 1..50) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Trending category limit must be between 1 and 50.",
            )
        }

        return wallpaperRepository.getTrendingCategories(source = source, limit = limit)
    }
}
