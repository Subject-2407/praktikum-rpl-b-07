package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchQuery
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.WallpaperRepository

/** Sends search analytics events without affecting the main search flow. */
class LogSearchEventUseCase(private val wallpaperRepository: WallpaperRepository) {
    suspend operator fun invoke(
        query: String,
        source: WallpaperSource,
        resultCount: Int? = null,
    ): ScapesResult<Unit> {
        val normalizedQuery =
            if (query.trim().startsWith("#")) {
                query.trim().take(255)
            } else {
                SearchQuery.normalize(query)
                    ?: return ScapesResult.Error(
                        code = ErrorCode.VALIDATION,
                        message = "Search query cannot be empty.",
                    )
            }

        if (normalizedQuery.removePrefix("#").length < 2) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Search query must be at least 2 characters.",
            )
        }

        return wallpaperRepository.logSearchEvent(
            query = normalizedQuery,
            source = source,
            resultCount = resultCount,
        )
    }
}
