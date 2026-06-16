package com.scapes.data.repository

import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.data.remote.api.WallpaperDownload
import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository
import com.scapes.platform.FileSystemProvider
import com.scapes.platform.WallpaperApplier

class ExternalWallpaperRepository(
    private val externalWallpaperApi: ExternalWallpaperApi,
    private val settingsRepository: SettingsRepository? = null,
    private val fileSystemProvider: FileSystemProvider? = null,
    private val wallpaperApplier: WallpaperApplier? = null,
) : WallpaperRepository {
    override suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>> =
        externalWallpaperApi.getWallpaperSources()

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> =
        externalWallpaperApi.searchWallpapers(
            query = query,
            page = page + 1,
            source = source,
            targetDevice = targetDevice,
        )

    override suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper> =
        when (val download = externalWallpaperApi.downloadWallpaper(wallpaper)) {
            is ScapesResult.Error -> download
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success -> saveDownloadedFile(wallpaper, download.data)
        }

    override suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
    ): ScapesResult<Unit> {
        val applier =
            wallpaperApplier
                ?: return platformUnavailable("Wallpaper apply requires platform wiring.")

        return when (val download = externalWallpaperApi.downloadWallpaper(wallpaper)) {
            is ScapesResult.Error -> download
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success -> {
                when (val saved = saveDownloadedFile(wallpaper, download.data)) {
                    is ScapesResult.Error -> saved
                    ScapesResult.Loading -> ScapesResult.Loading
                    is ScapesResult.Success ->
                        applier
                            .apply(download.data.bytes, target)
                            .fold(
                                onSuccess = { ScapesResult.Success(Unit) },
                                onFailure = { throwable ->
                                    ScapesResult.Error(
                                        code = ErrorCode.UNSUPPORTED_PLATFORM,
                                        message =
                                            "Wallpaper apply failed: ${throwable.message.orEmpty()}",
                                    )
                                },
                            )
                }
            }
        }
    }

    override suspend fun validateApiKey(
        source: WallpaperSource,
        apiKey: String,
    ): ScapesResult<Unit> = externalWallpaperApi.validateApiKey(source, apiKey)

    override fun invalidateSource(source: WallpaperSource) {
        externalWallpaperApi.invalidateSource(source)
    }

    private suspend fun saveDownloadedFile(
        wallpaper: Wallpaper,
        download: WallpaperDownload,
    ): ScapesResult<Wallpaper> {
        val fileSystem =
            fileSystemProvider
                ?: return platformUnavailable(
                    "Saving wallpapers requires file-system platform wiring."
                )
        val settings =
            settingsRepository
                ?: return platformUnavailable(
                    "Saving wallpapers requires settings repository wiring."
                )

        val downloadSettings =
            when (val result = settings.getDownloadSettings()) {
                is ScapesResult.Error -> return result
                ScapesResult.Loading -> return ScapesResult.Loading
                is ScapesResult.Success -> result.data
            }

        val folder =
            destinationFolder(wallpaper, downloadSettings.folderPath, downloadSettings.organization)
        if (!fileSystem.createDirectoryIfAbsent(folder) || !fileSystem.hasWriteAccess(folder)) {
            return ScapesResult.Error(
                code = ErrorCode.STORAGE,
                message = "Download folder is not writable.",
            )
        }

        return fileSystem
            .saveFile(
                path = folder,
                filename = downloadFilename(wallpaper, download.extension),
                bytes = download.bytes,
            )
            .fold(
                onSuccess = { localPath ->
                    ScapesResult.Success(wallpaper.copy(localPath = localPath))
                },
                onFailure = { throwable ->
                    ScapesResult.Error(
                        code = ErrorCode.STORAGE,
                        message = "Wallpaper could not be saved: ${throwable.message.orEmpty()}",
                    )
                },
            )
    }

    private fun destinationFolder(
        wallpaper: Wallpaper,
        rootPath: String,
        organization: DownloadOrganization,
    ): String {
        val cleanRoot = rootPath.trim().trimEnd('/', '\\')
        val child =
            when (organization) {
                DownloadOrganization.BY_CATEGORY ->
                    wallpaper.category?.slug?.takeIf { it.isNotBlank() }
                        ?: wallpaper.source.name.lowercase()

                DownloadOrganization.BY_SOURCE -> wallpaper.source.name.lowercase()
                DownloadOrganization.NONE -> ""
            }

        return if (child.isBlank()) cleanRoot else "$cleanRoot/${safePathSegment(child)}"
    }

    private fun downloadFilename(wallpaper: Wallpaper, extension: String): String {
        val title = safePathSegment(wallpaper.title).ifBlank { "wallpaper" }
        val id =
            safePathSegment(wallpaper.id).takeLast(20).ifBlank { wallpaper.source.name.lowercase() }
        return "$title-$id.$extension"
    }

    private fun safePathSegment(raw: String): String =
        raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-')

    private fun <T> platformUnavailable(message: String): ScapesResult<T> =
        ScapesResult.Error(code = ErrorCode.UNSUPPORTED_PLATFORM, message = message)
}
