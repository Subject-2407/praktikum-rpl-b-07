package com.scapes.domain.usecase

import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.domain.repository.WallpaperRepository

/** Validates and stores a personal provider API key. */
class SaveApiKeyUseCase(
    private val apiKeyRepository: ApiKeyRepository,
    private val wallpaperRepository: WallpaperRepository,
) {
    suspend operator fun invoke(source: WallpaperSource, rawKey: String): ScapesResult<Unit> {
        val key = rawKey.trim()
        if (key.isBlank()) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "API key cannot be empty.",
            )
        }

        return when (val validation = wallpaperRepository.validateApiKey(source, key)) {
            is ScapesResult.Error -> validation
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success -> {
                val saveResult = apiKeyRepository.saveApiKey(ApiKey(source = source, value = key))
                if (saveResult is ScapesResult.Success) {
                    wallpaperRepository.invalidateSource(source)
                }
                saveResult
            }
        }
    }
}
