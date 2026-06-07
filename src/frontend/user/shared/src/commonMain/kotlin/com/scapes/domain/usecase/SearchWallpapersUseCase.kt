package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchQuery
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository

/**
 * Searches wallpapers through the currently selected source.
 */
class SearchWallpapersUseCase(
    private val wallpaperRepository: WallpaperRepository,
    private val settingsRepository: SettingsRepository,
) {
    /**
     * Searches for wallpapers matching [query].
     *
     * @param query user-entered search text.
     * @param page zero-based page index.
     */
    suspend operator fun invoke(
        query: String,
        page: Int = 0,
    ): ScapesResult<List<Wallpaper>> {
        val normalizedQuery =
            SearchQuery.normalize(query)
                ?: return ScapesResult.Error(
                    code = ErrorCode.VALIDATION,
                    message = "Search query cannot be empty.",
                )

        if (page < 0) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Search page cannot be negative.",
            )
        }

        return when (val selectedSource = settingsRepository.getSelectedSource()) {
            is ScapesResult.Error -> selectedSource
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success ->
                wallpaperRepository.searchWallpapers(
                    query = normalizedQuery,
                    page = page,
                    source = selectedSource.data,
                )
        }
    }
}
