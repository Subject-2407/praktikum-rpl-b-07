package com.scapes.domain.usecase

import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.SettingsRepository

/** Persists the active wallpaper provider. */
class SwitchWallpaperSourceUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(source: WallpaperSource): ScapesResult<Unit> =
        settingsRepository.setSelectedSource(source)
}
