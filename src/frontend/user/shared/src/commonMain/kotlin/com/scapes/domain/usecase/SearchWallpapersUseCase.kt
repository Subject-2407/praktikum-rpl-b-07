package com.scapes.domain.usecase

import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchQuery
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository

/** Searches wallpapers through the currently selected source. */
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
        source: WallpaperSource? = null,
        targetDevice: TargetDevice = TargetDevice.DESKTOP,
        categorySlug: String? = null,
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

        val activeSource =
            source
                ?: when (val selectedSource = settingsRepository.getSelectedSource()) {
                    is ScapesResult.Error -> return selectedSource
                    ScapesResult.Loading -> return ScapesResult.Loading
                    is ScapesResult.Success -> selectedSource.data
                }

        return wallpaperRepository
            .searchWallpapers(
                query = normalizedQuery,
                page = page,
                source = activeSource,
                targetDevice = targetDevice,
                categorySlug = categorySlug,
            )
            .let { result ->
                if (result is ScapesResult.Success) {
                    ScapesResult.Success(
                        result.data.filter { wallpaper -> wallpaper.targetDevice == targetDevice }
                    )
                } else {
                    result
                }
            }
    }
}
