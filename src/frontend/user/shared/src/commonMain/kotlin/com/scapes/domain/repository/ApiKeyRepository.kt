package com.scapes.domain.repository

import com.scapes.domain.model.ApiKey
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource

/** Secure persistence boundary for provider API keys. */
interface ApiKeyRepository {
    /** Saves [apiKey] after provider validation. */
    suspend fun saveApiKey(apiKey: ApiKey): ScapesResult<Unit>

    /** Loads the key associated with [source]. */
    suspend fun getApiKey(source: WallpaperSource): ScapesResult<ApiKey?>

    /** Removes the key associated with [source]. */
    suspend fun removeApiKey(source: WallpaperSource): ScapesResult<Unit>
}
