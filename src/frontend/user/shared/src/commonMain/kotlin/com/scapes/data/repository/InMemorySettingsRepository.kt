package com.scapes.data.repository

import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.SettingsRepository

/** Shared fallback settings store until platform persistence is wired. */
class InMemorySettingsRepository(
    selectedSource: WallpaperSource = WallpaperSource.SCAPES_API,
    downloadSettings: DownloadSettings =
        DownloadSettings(folderPath = "Scapes", organization = DownloadOrganization.BY_CATEGORY),
) : SettingsRepository {
    private var activeSource = selectedSource
    private var activeDownloadSettings = downloadSettings

    override suspend fun getSelectedSource(): ScapesResult<WallpaperSource> =
        ScapesResult.Success(activeSource)

    override suspend fun setSelectedSource(source: WallpaperSource): ScapesResult<Unit> {
        activeSource = source
        return ScapesResult.Success(Unit)
    }

    override suspend fun getDownloadSettings(): ScapesResult<DownloadSettings> =
        ScapesResult.Success(activeDownloadSettings)

    override suspend fun setDownloadSettings(settings: DownloadSettings): ScapesResult<Unit> {
        activeDownloadSettings = settings
        return ScapesResult.Success(Unit)
    }
}
