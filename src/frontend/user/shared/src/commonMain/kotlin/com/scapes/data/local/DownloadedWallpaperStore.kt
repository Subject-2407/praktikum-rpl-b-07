package com.scapes.data.local

import com.scapes.data.local.db.ScapesDatabase
import com.scapes.domain.model.TargetDevice
import com.scapes.domain.model.Wallpaper
import com.scapes.domain.model.WallpaperSource
import kotlinx.serialization.Serializable

/** Local SQLDelight store for downloaded wallpaper metadata. */
class DownloadedWallpaperStore(private val database: ScapesDatabase) {
    private val queries = database.scapesDatabaseQueries

    /** Loads downloaded wallpapers ordered from newest to oldest. */
    fun getAll(): List<Wallpaper> =
        queries.selectAllDownloaded(::mapDownloadedWallpaper).executeAsList()

    /** Finds one downloaded wallpaper by [id]. */
    fun getById(id: String): Wallpaper? =
        queries.selectDownloadedById(id, ::mapDownloadedWallpaper).executeAsOneOrNull()

    /** Inserts or replaces downloaded metadata for [wallpaper]. */
    fun upsert(wallpaper: Wallpaper, localPath: String, downloadedAtEpochMillis: Long) {
        queries.upsertDownloaded(
            id = wallpaper.id,
            title = wallpaper.title,
            source = wallpaper.source.name,
            preview_url = localPath,
            remote_url = wallpaper.fullImageUrl,
            local_path = localPath,
            description = wallpaper.description,
            author_name = wallpaper.authorName,
            width = wallpaper.width.toLong(),
            height = wallpaper.height.toLong(),
            target_device = wallpaper.targetDevice.name,
            downloaded_at_epoch_millis = downloadedAtEpochMillis,
        )
    }

    /** Removes downloaded metadata for [id]. */
    fun deleteById(id: String) {
        queries.deleteDownloadedById(id)
    }

    /** Replaces all downloaded metadata with [wallpapers]. */
    fun replaceAll(wallpapers: List<StoredDownloadedWallpaper>) {
        queries.transaction {
            queries.deleteAllDownloaded()
            wallpapers.forEach { wallpaper ->
                queries.upsertDownloaded(
                    id = wallpaper.id,
                    title = wallpaper.title,
                    source = wallpaper.source,
                    preview_url = wallpaper.previewUrl,
                    remote_url = wallpaper.remoteUrl,
                    local_path = wallpaper.localPath,
                    description = wallpaper.description,
                    author_name = wallpaper.authorName,
                    width = wallpaper.width.toLong(),
                    height = wallpaper.height.toLong(),
                    target_device = wallpaper.targetDevice,
                    downloaded_at_epoch_millis = wallpaper.downloadedAtEpochMillis,
                )
            }
        }
    }

    private fun mapDownloadedWallpaper(
        id: String,
        title: String,
        source: String,
        previewUrl: String,
        remoteUrl: String,
        localPath: String,
        description: String?,
        authorName: String?,
        width: Long,
        height: Long,
        targetDevice: String,
        downloadedAtEpochMillis: Long,
    ): Wallpaper =
        Wallpaper(
            id = id,
            title = title,
            source =
                runCatching { WallpaperSource.valueOf(source) }
                    .getOrDefault(WallpaperSource.SCAPES_API),
            previewUrl = previewUrl,
            fullImageUrl = remoteUrl,
            description = description,
            authorName = authorName,
            width = width.toInt(),
            height = height.toInt(),
            targetDevice =
                runCatching { TargetDevice.valueOf(targetDevice) }
                    .getOrDefault(TargetDevice.DESKTOP),
            localPath = localPath,
        )
}

/** Persisted downloaded wallpaper metadata used for legacy catalog migration. */
@Serializable
data class StoredDownloadedWallpaper(
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
)
