package com.scapes.data.repository

import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.DownloadSettings
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.repository.SettingsRepository
import com.scapes.platform.EncryptedStorage
import com.scapes.platform.FileSystemProvider

private const val SelectedSourceKey = "selected_source"
private const val DownloadFolderKey = "download_folder"
private const val DownloadOrganizationKey = "download_organization"

/** Persists user settings in platform-backed local settings storage. */
class PersistentSettingsRepository(
    private val storage: EncryptedStorage,
    private val fileSystemProvider: FileSystemProvider,
) : SettingsRepository {
    override suspend fun getSelectedSource(): ScapesResult<WallpaperSource> =
        runCatching {
                ScapesResult.Success(
                    storage.getString(SelectedSourceKey)?.let(::parseSource)
                        ?: WallpaperSource.SCAPES_API
                )
            }
            .getOrElse(::storageError)

    override suspend fun setSelectedSource(source: WallpaperSource): ScapesResult<Unit> =
        runCatching {
                storage.putString(SelectedSourceKey, source.name)
                ScapesResult.Success(Unit)
            }
            .getOrElse(::storageError)

    override suspend fun getDownloadSettings(): ScapesResult<DownloadSettings> =
        runCatching {
                ScapesResult.Success(
                    DownloadSettings(
                        folderPath = storage.getString(DownloadFolderKey) ?: defaultDownloadPath(),
                        organization =
                            storage.getString(DownloadOrganizationKey)?.let(::parseOrganization)
                                ?: DownloadOrganization.BY_CATEGORY,
                    )
                )
            }
            .getOrElse(::storageError)

    override suspend fun setDownloadSettings(settings: DownloadSettings): ScapesResult<Unit> {
        val folderPath = settings.folderPath.trim()
        if (
            !fileSystemProvider.createDirectoryIfAbsent(folderPath) ||
                !fileSystemProvider.hasWriteAccess(folderPath)
        ) {
            return ScapesResult.Error(
                code = ErrorCode.STORAGE,
                message = "Download folder is not writable.",
            )
        }

        return runCatching {
                storage.putString(DownloadFolderKey, folderPath)
                storage.putString(DownloadOrganizationKey, settings.organization.name)
                ScapesResult.Success(Unit)
            }
            .getOrElse(::storageError)
    }

    private fun defaultDownloadPath(): String =
        runCatching { fileSystemProvider.getDefaultDownloadPath() }.getOrDefault("Scapes")

    private fun parseSource(raw: String): WallpaperSource? =
        runCatching { WallpaperSource.valueOf(raw) }.getOrNull()

    private fun parseOrganization(raw: String): DownloadOrganization? =
        runCatching { DownloadOrganization.valueOf(raw) }.getOrNull()

    private fun storageError(throwable: Throwable): ScapesResult.Error =
        ScapesResult.Error(
            code = ErrorCode.STORAGE,
            message = "Settings could not be saved: ${throwable.message.orEmpty()}",
        )
}
