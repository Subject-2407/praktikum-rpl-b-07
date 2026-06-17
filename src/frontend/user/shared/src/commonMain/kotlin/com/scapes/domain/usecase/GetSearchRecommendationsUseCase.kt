package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.SearchQuery
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.WallpaperRepository

/** Loads live recommendations for the search box. */
class GetSearchRecommendationsUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(
        query: String,
        source: WallpaperSource,
        limit: Int = 10,
    ): ScapesResult<List<SearchRecommendation>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return ScapesResult.Success(emptyList())
        }

        if (limit !in 1..50) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Search recommendation limit must be between 1 and 50.",
            )
        }

        val normalizedQuery =
            if (trimmedQuery.startsWith("#")) {
                "#${trimmedQuery.drop(1).trim().take(SearchQuery.MAX_LENGTH)}"
            } else {
                SearchQuery.normalize(trimmedQuery)
                    ?: return ScapesResult.Success(emptyList())
            }

        return wallpaperRepository.getSearchRecommendations(
            query = normalizedQuery,
            source = source,
            limit = limit,
        )
    }
}
