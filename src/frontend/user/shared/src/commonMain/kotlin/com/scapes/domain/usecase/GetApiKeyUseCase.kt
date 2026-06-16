package com.scapes.domain.usecase

import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository

/** Loads a personal provider API key. */
class GetApiKeyUseCase(private val apiKeyRepository: ApiKeyRepository) {
    suspend operator fun invoke(source: WallpaperSource): ScapesResult<ApiKey?> =
        apiKeyRepository.getApiKey(source)
}
