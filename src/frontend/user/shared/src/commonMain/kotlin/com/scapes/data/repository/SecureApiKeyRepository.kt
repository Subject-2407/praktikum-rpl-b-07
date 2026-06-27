package com.scapes.data.repository

import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.ApiKeyRepository
import com.scapes.platform.EncryptedStorage

/** Persists user-owned provider keys inside platform-secure storage. */
class SecureApiKeyRepository(private val encryptedStorage: EncryptedStorage) : ApiKeyRepository {
    override suspend fun saveApiKey(apiKey: ApiKey): ScapesResult<Unit> {
        val storageKey = storageKey(apiKey.source) ?: return unsupportedSource()
        val trimmedKey = apiKey.value.trim()
        if (trimmedKey.isBlank()) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "API key cannot be empty.",
            )
        }

        return runCatching {
                encryptedStorage.putString(storageKey, trimmedKey)
                ScapesResult.Success(Unit)
            }
            .getOrElse { throwable -> storageError(throwable) }
    }

    override suspend fun getApiKey(source: WallpaperSource): ScapesResult<ApiKey?> {
        val storageKey = storageKey(source) ?: return unsupportedSource()

        return runCatching {
                ScapesResult.Success(
                    encryptedStorage
                        .getString(storageKey)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?.let { ApiKey(source = source, value = it) }
                )
            }
            .getOrElse { throwable -> storageError(throwable) }
    }

    override suspend fun removeApiKey(source: WallpaperSource): ScapesResult<Unit> {
        val storageKey = storageKey(source) ?: return unsupportedSource()

        return runCatching {
                encryptedStorage.remove(storageKey)
                ScapesResult.Success(Unit)
            }
            .getOrElse { throwable -> storageError(throwable) }
    }

    private fun storageKey(source: WallpaperSource): String? =
        when (source) {
            WallpaperSource.PEXELS -> "api_key_pexels"
            WallpaperSource.UNSPLASH -> "api_key_unsplash"
            WallpaperSource.PIXABAY -> "api_key_pixabay"
            WallpaperSource.SCAPES_API -> null
        }

    private fun unsupportedSource(): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.UNSUPPORTED_PLATFORM,
            message = "Scapes does not use a personal API key.",
        )

    private fun storageError(throwable: Throwable): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.STORAGE,
            message = "Secure API key storage is unavailable: ${throwable.message.orEmpty()}",
        )
}
