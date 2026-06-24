package com.scapes.data.repository

import com.scapes.data.local.DownloadedWallpaperStore
import com.scapes.data.local.StoredDownloadedWallpaper
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
import kotlinx.serialization.json.Json

private const val LegacyDownloadedWallpaperCatalogKey = "downloaded_wallpaper_catalog_v1"

class ExternalWallpaperRepository(
    private val externalWallpaperApi: ExternalWallpaperApi,
    private val downloadedWallpaperStore: DownloadedWallpaperStore? = null,
    private val legacyCatalogStorage: EncryptedStorage? = null,
    private val settingsRepository: SettingsRepository? = null,
    private val fileSystemProvider: FileSystemProvider? = null,
    private val wallpaperApplier: WallpaperApplier? = null,
) : WallpaperRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private var migratedLegacyCatalog = false

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
        val store =
            downloadedWallpaperStore
                ?: return platformUnavailable(
                    "Collections require local database wiring."
                )

        migrateLegacyDownloadedCatalog()

        val catalog = store.getAll()
        val validEntries =
            catalog.filter { entry ->
                entry.localPath?.let { localPath -> fileSystem.fileExists(localPath) } == true
            }
        if (validEntries.size != catalog.size) {
            val validIds = validEntries.map { wallpaper -> wallpaper.id }.toSet()
            catalog
                .filterNot { wallpaper -> wallpaper.id in validIds }
                .forEach { wallpaper -> store.deleteById(wallpaper.id) }
        }

        return ScapesResult.Success(validEntries)
    }

    override suspend fun searchWallpapers(
        query: String,
        page: Int,
        source: WallpaperSource,
        targetDevice: TargetDevice,
        categorySlug: String?,
        limit: Int?,
    ): ScapesResult<List<Wallpaper>> =
        externalWallpaperApi.searchWallpapers(
            query = query,
            page = page + 1,
            source = source,
            targetDevice = targetDevice,
            categorySlug = categorySlug,
            limit = limit,
        )

    override suspend fun saveWallpaper(wallpaper: Wallpaper, onProgress: suspend (Float) -> Unit): ScapesResult<Wallpaper> {
        resolveExistingDownloadedWallpaper(wallpaper)?.let { existingWallpaper ->
            onProgress(1f)
            return ScapesResult.Success(existingWallpaper)
        }

        return when (val download = externalWallpaperApi.downloadWallpaper(wallpaper, onProgress)) {
            is ScapesResult.Error -> download
            ScapesResult.Loading -> ScapesResult.Loading
            is ScapesResult.Success -> saveDownloadedFile(wallpaper, download.data)
        }
    }

    override suspend fun applyWallpaper(
        wallpaper: Wallpaper,
        target: ApplyTarget,
        onProgress: suspend (Float) -> Unit,
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
                onProgress(1f)
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

        return when (val download = externalWallpaperApi.downloadWallpaper(wallpaper, onProgress)) {
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
        val store = downloadedWallpaperStore ?: return null

        migrateLegacyDownloadedCatalog()

        wallpaper.localPath
            ?.takeIf { localPath -> fileSystem.fileExists(localPath) }
            ?.let { localPath -> return wallpaper.copy(localPath = localPath) }

        return store
            .getById(wallpaper.id)
            ?.takeIf { storedWallpaper -> fileSystem.fileExists(storedWallpaper.localPath.orEmpty()) }
    }

    private fun migrateLegacyDownloadedCatalog() {
        if (migratedLegacyCatalog) {
            return
        }
        migratedLegacyCatalog = true

        val storage = legacyCatalogStorage ?: return
        val store = downloadedWallpaperStore ?: return
        if (store.getAll().isNotEmpty()) {
            storage.remove(LegacyDownloadedWallpaperCatalogKey)
            return
        }

        val rawCatalog =
            runCatching { storage.getString(LegacyDownloadedWallpaperCatalogKey) }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: return

        val entries =
            runCatching {
                json.decodeFromString<List<StoredDownloadedWallpaper>>(rawCatalog)
            }
            .getOrDefault(emptyList())

        if (entries.isNotEmpty()) {
            store.replaceAll(entries)
        }
        storage.remove(LegacyDownloadedWallpaperCatalogKey)
    }

    private fun upsertDownloadedMetadata(wallpaper: Wallpaper, localPath: String) {
        val store = downloadedWallpaperStore ?: return
        store.upsert(
            wallpaper = wallpaper,
            localPath = localPath,
            downloadedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    private fun safePathSegment(raw: String): String =
        raw.trim().lowercase().replace(Regex("[^a-z0-9._-]+"), "-").trim('-')

    private fun <T> platformUnavailable(message: String): ScapesResult<T> =
        ScapesResult.Error(code = ErrorCode.UNSUPPORTED_PLATFORM, message = message)
}
