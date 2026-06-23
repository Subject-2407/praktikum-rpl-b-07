package com.scapes.platform

import android.app.DownloadManager
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

actual class FileSystemProvider : KoinComponent {
    private val context: Context by inject()
    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    actual fun getDefaultDownloadPath(): String = "Download"

    actual fun createDirectoryIfAbsent(path: String): Boolean {
        val dir = if (path.startsWith("/")) {
            File(path)
        } else {
            val root = path.substringBefore("/").let { segment ->
                when {
                    segment.equals("Pictures", ignoreCase = true) -> Environment.DIRECTORY_PICTURES
                    segment.equals("DCIM", ignoreCase = true) -> Environment.DIRECTORY_DCIM
                    else -> Environment.DIRECTORY_DOWNLOADS
                }
            }
            val subPath = path.substringAfter("/", "")
            File(Environment.getExternalStoragePublicDirectory(root), subPath)
        }
        return dir.exists() || dir.mkdirs()
    }

    actual fun saveFile(path: String, filename: String, bytes: ByteArray): Result<String> {
        return try {
            val subPath = "Scapes/$filename"
            val directory = when {
                path.contains("Pictures", ignoreCase = true) -> Environment.DIRECTORY_PICTURES
                path.contains("DCIM", ignoreCase = true) -> Environment.DIRECTORY_DCIM
                else -> Environment.DIRECTORY_DOWNLOADS
            }

            val file = File(Environment.getExternalStoragePublicDirectory(directory), subPath)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)

            // DownloadManager for system awareness
            val url = AndroidDownloadRegistry.consumeUrl(filename)
            if (url != null) {
                try {
                    val request = DownloadManager.Request(url.toUri())
                        .setTitle(filename)
                        .setDescription("Download complete")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    downloadManager.enqueue(request)
                } catch (_: Exception) { }
            }

            // index file to gallery
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                arrayOf("image/jpeg"),
                null
            )

            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual fun readFile(path: String): Result<ByteArray> = runCatching {
        File(path).inputStream().use { it.readBytes() }
    }

    actual fun fileExists(path: String): Boolean {
        if (File(path).exists()) return true

        return try {
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DATA} = ?"
            val selectionArgs = arrayOf(path)

            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                cursor.count > 0
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    actual fun listFiles(path: String): List<String> =
        File(path).listFiles()?.map { it.absolutePath } ?: emptyList()

    actual fun deleteFile(path: String): Boolean = File(path).delete()

    actual fun hasWriteAccess(path: String): Boolean = true
}
