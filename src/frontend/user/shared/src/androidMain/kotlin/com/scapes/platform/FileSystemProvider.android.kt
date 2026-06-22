package com.scapes.platform

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import androidx.core.net.toUri

actual class FileSystemProvider : KoinComponent {
    private val context: Context by inject()
    private val downloadManager by lazy {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    actual fun getDefaultDownloadPath(): String = "Download"

    actual fun createDirectoryIfAbsent(path: String): Boolean = true

    actual fun saveFile(path: String, filename: String, bytes: ByteArray): Result<String> {
        // Grab URL from side-channel cache using filename
        val url = AndroidDownloadRegistry.consumeUrl(filename)
            ?: return Result.failure(Exception("Wallpaper URL not found in registry for ID: $filename"))

        return try {
            val request = DownloadManager.Request(url.toUri())
                .setTitle(filename)
                .setDescription("Downloading wallpaper from Scapes")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            // Dynamic route mapping based on target path
            val subPath = "Scapes/${filename}"
            val directory = when {
                path.contains("Pictures", ignoreCase = true) -> Environment.DIRECTORY_PICTURES
                path.contains("DCIM", ignoreCase = true) -> Environment.DIRECTORY_DCIM
                else -> Environment.DIRECTORY_DOWNLOADS
            }

            request.setDestinationInExternalPublicDir(directory, subPath)
            downloadManager.enqueue(request)

            // Return the predicted public path for real-time DB & UI sync
            val predictedFile = File(Environment.getExternalStoragePublicDirectory(directory), subPath)
            Result.success(predictedFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual fun readFile(path: String): Result<ByteArray> = runCatching {
        File(path).readBytes()
    }

    actual fun fileExists(path: String): Boolean = File(path).exists()

    actual fun listFiles(path: String): List<String> =
        File(path).listFiles()?.map { it.absolutePath } ?: emptyList()

    actual fun deleteFile(path: String): Boolean = File(path).delete()

    actual fun hasWriteAccess(path: String): Boolean = true
}
