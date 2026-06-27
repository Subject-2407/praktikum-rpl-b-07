package com.scapes.domain.usecase

import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.repository.SettingsRepository

/** Loads download folder preferences for settings UI and save flows. */
class GetDownloadSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(): ScapesResult<DownloadSettings> =
        settingsRepository.getDownloadSettings()
}
