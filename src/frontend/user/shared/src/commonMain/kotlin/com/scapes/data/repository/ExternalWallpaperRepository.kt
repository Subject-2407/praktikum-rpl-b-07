package com.scapes.data.repository

import com.scapes.data.remote.api.ExternalWallpaperApi
import com.scapes.data.remote.api.WallpaperDownload
import com.scapes.domain.model.ApplyTarget
import com.scapes.domain.model.DownloadOrganization
import com.scapes.domain.model.ErrorCode
import com.scapes.domain.model.ScapesResult
import com.scapes.domain.model.SearchRecommendation
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.TrendingCategory
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperCategory
import com.scapes.domain.model.WallpaperSource
import com.scapes.domain.model.WallpaperSourceInfo
import com.scapes.domain.repository.SettingsRepository
import com.scapes.domain.repository.WallpaperRepository
import com.scapes.platform.EncryptedStorage
import com.scapes.platform.FileSystemProvider
import com.scapes.platform.WallpaperApplier
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val DownloadedWallpaperCatalogKey = "downloaded_wallpaper_catalog_v1"

class ExternalWallpaperRepository(
    private val externalWallpaperApi: ExternalWallpaperApi,
    private val storage: EncryptedStorage? = null,
    private val settingsRepository: SettingsRepository? = null,
    private val fileSystemProvider: FileSystemProvider? = null,
    private val wallpaperApplier: WallpaperApplier? = null,
) : WallpaperRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getWallpaperSources(): ScapesResult<List<WallpaperSourceInfo>> =
        externalWallpaperApi.getWallpaperSources()

    override suspend fun getCategories(): ScapesResult<List<WallpaperCategory>> =
        externalWallpaperApi.getCategories()

    override suspend fun getTrendingCategories(
        source: WallpaperSource,
        limit: Int,
    ): ScapesResult<List<TrendingCategory>> =
        externalWallpaperApi.getTrendingCategories(source = source, limit = limit)

    override suspend fun getSearchRecommendations(
        query: String,
        source: WallpaperSource,
        limit: Int,
    ): ScapesResult<List<SearchRecommendation>> =
        externalWallpaperApi.getSearchRecommendations(query = query, source = source, limit = limit)

    override suspend fun getFeaturedWallpapers(
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice,
    ): ScapesResult<List<Wallpaper>> =
        externalWallpaperApi.getFeaturedWallpapers(
            page = page + 1,
            source = source,
            targetDevice = targetDevice,
        )

    override suspend fun getDownloadedWallpapers(): ScapesResult<List<Wallpaper>> {
        val fileSystem =
            fileSystemProvider
                ?: return platformUnavailable(
                    "Collections require file-system platform wiring."
                )

        val catalog = loadDownloadedCatalog()
        val validEntries = catalog.filter { entry -> fileSystem.fileExists(entry.localPath) }
        if (validEntries.size != catalog.size) {
            storeDownloadedCatalog(validEntries)
        }

        return ScapesResult.Success(validEntries.map { entry -> entry.toWallpaper() })
    }

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice,
        categorySlug: String?,
    ): ScapesResult<List<Wallpaper>> =
        externalWallpaperApi.searchWallpapers(
            query = query,
            page = page + 1,
            source = source,
            targetDevice = targetDevice,
            categorySlug = categorySlug,
        )

    override suspend fun saveWallpaper(wallpaper: Wallpaper): ScapesResult<Wallpaper> {
        resolveExistingDownloadedWallpaper(wallpaper)?.let { existingWallpaper ->
            return ScapesResult.Success(existingWallpaper)
        }

        return when (val download = externalWallpaperApi.downloadWallpaper(wallpaper)) {
            is ScapesResult.Error -> download
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success -> saveDownloadedFile(wallpaper, download.data)
        }
    }

    override suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
    ): ScapesResult<Unit> {
        val applier =
            wallpaperApplier
                ?: return platformUnavailable("Wallpaper apply requires platform wiring.")

        resolveExistingDownloadedWallpaper(wallpaper)?.let { localWallpaper ->
            val bytesResult =
                fileSystemProvider
                    ?.readFile(localWallpaper.localPath.orEmpty())
                    ?.getOrNull()
            if (bytesResult != null) {
                return applier
                    .apply(bytesResult, target)
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

    override suspend fun logSearchEvent(
        query: String,
        source: WallpaperSource,
        resultCount: Int?,
    ): ScapesResult<Unit> =
        externalWallpaperApi.logSearchEvent(
            query = query,
            source = source,
            resultCount = resultCount,
        )

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

        val existingLocalPath = wallpaper.localPath?.takeIf { localPath -> fileSystem.fileExists(localPath) }
        if (existingLocalPath != null) {
            upsertDownloadedMetadata(wallpaper, existingLocalPath)
            return ScapesResult.Success(wallpaper.copy(localPath = existingLocalPath))
        }

        return fileSystem
            .saveFile(
                path = folder,
                filename = downloadFilename(wallpaper, download.extension),
                bytes = download.bytes,
            )
            .fold(
                onSuccess = { localPath ->
                    upsertDownloadedMetadata(wallpaper, localPath)
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

    private fun resolveExistingDownloadedWallpaper(wallpaper: Wallpaper): Wallpaper? {
        val fileSystem = fileSystemProvider ?: return null

        wallpaper.localPath
            ?.takeIf { localPath -> fileSystem.fileExists(localPath) }
            ?.let { localPath -> return wallpaper.copy(localPath = localPath) }

        val entry =
            loadDownloadedCatalog().firstOrNull { storedWallpaper ->
                storedWallpaper.id == wallpaper.id && fileSystem.fileExists(storedWallpaper.localPath)
            }
        return entry?.toWallpaper()
    }

    private fun loadDownloadedCatalog(): List<StoredDownloadedWallpaper> {
        val storage = storage ?: return emptyList()
        val rawCatalog =
            runCatching { storage.getString(DownloadedWallpaperCatalogKey) }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: return emptyList()

        return runCatching {
                json.decodeFromString<List<StoredDownloadedWallpaper>>(rawCatalog)
            }
            .getOrDefault(emptyList())
    }

    private fun storeDownloadedCatalog(entries: List<StoredDownloadedWallpaper>) {
        val storage = storage ?: return
        runCatching {
            storage.putString(
                DownloadedWallpaperCatalogKey,
                json.encodeToString(entries.sortedByDescending { entry -> entry.downloadedAtEpochMillis }),
            )
        }
    }

    private fun upsertDownloadedMetadata(wallpaper: Wallpaper, localPath: String) {
        val updatedEntry =
            StoredDownloadedWallpaper(
                id = wallpaper.id,
                title = wallpaper.title,
                source = wallpaper.source.name,
                previewUrl = localPath,
                remoteUrl = wallpaper.fullImageUrl,
                localPath = localPath,
                description = wallpaper.description,
                authorName = wallpaper.authorName,
                width = wallpaper.width,
                height = wallpaper.height,
                targetDevice = wallpaper.targetDevice.name,
                downloadedAtEpochMillis = System.currentTimeMillis(),
            )

        val existingEntries = loadDownloadedCatalog().filterNot { entry -> entry.id == wallpaper.id }
        storeDownloadedCatalog(existingEntries + updatedEntry)
    }

    private fun safePathSegment(raw: String): String =
        raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-')

    private fun <T> platformUnavailable(message: String): ScapesResult<T> =
        ScapesResult.Error(code = ErrorCode.UNSUPPORTED_PLATFORM, message = message)

    @Serializable
    private data class StoredDownloadedWallpaper(
        val id: String,
        val title: String,
        val source: String,
        val previewUrl: String,
        val remoteUrl: String,
        val localPath: String,
        val description: String? = null,
        val authorName: String? = null,
        val width: Int = 0,
        val height: Int = 0,
        val targetDevice: String = TargetDevice.DESKTOP.name,
        val downloadedAtEpochMillis: Long,
    ) {
        fun toWallpaper(): Wallpaper =
            Wallpaper(
                id = id,
                title = title,
                source = runCatching { WallpaperSource.valueOf(source) }.getOrDefault(WallpaperSource.SCAPES_API),
                previewUrl = previewUrl,
                fullImageUrl = remoteUrl,
                description = description,
                authorName = authorName,
                width = width,
                height = height,
                targetDevice =
                    runCatching { TargetDevice.valueOf(targetDevice) }
                        .getOrDefault(TargetDevice.DESKTOP),
                localPath = localPath,
            )
    }
}
