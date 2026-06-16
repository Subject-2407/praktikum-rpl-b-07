package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.domain.repository.WallpaperRepository

/** Removes a personal provider API key. */
class RemoveApiKeyUseCase(
    private val apiKeyRepository: ApiKeyRepository,
    private val wallpaperRepository: WallpaperRepository,
) {
    suspend operator fun invoke(source: WallpaperSource): ScapesResult<Unit> =
        apiKeyRepository.removeApiKey(source).also { result ->
            if (result is ScapesResult.Success) {
                wallpaperRepository.invalidateSource(source)
            }
        }
}
