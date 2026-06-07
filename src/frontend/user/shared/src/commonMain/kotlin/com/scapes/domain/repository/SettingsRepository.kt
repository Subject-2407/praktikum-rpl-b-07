package com.scapes.domain.repository

import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource

/**
 * User preferences boundary.
 */
interface SettingsRepository {
    /**
     * Loads the currently selected wallpaper source.
     */
    suspend fun getSelectedSource(): ScapesResult<WallpaperSource>

    /**
     * Persists the active wallpaper [source].
     */
    suspend fun setSelectedSource(source: WallpaperSource): ScapesResult<Unit>

    /**
     * Loads download folder and organization settings.
     */
    suspend fun getDownloadSettings(): ScapesResult<DownloadSettings>

    /**
     * Persists download folder and organization [settings].
     */
    suspend fun setDownloadSettings(settings: DownloadSettings): ScapesResult<Unit>
}
