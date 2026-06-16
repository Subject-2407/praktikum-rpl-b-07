package com.scapes.domain.usecase

import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.repository.SettingsRepository

/** Validates and stores download folder preferences. */
class UpdateDownloadSettingsUseCase(private val settingsRepository: SettingsRepository) {
    suspend operator fun invoke(settings: DownloadSettings): ScapesResult<Unit> {
        if (settings.folderPath.trim().isBlank()) {
            return ScapesResult.Error(
                code = ErrorCode.VALIDATION,
                message = "Download folder cannot be empty.",
            )
        }

        return settingsRepository.setDownloadSettings(
            settings.copy(folderPath = settings.folderPath.trim())
        )
    }
}
