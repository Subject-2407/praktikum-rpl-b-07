package com.scapes.platform

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Android file storage for wallpaper bytes downloaded by the shared Ktor API. */
actual class FileSystemProvider : KoinComponent {
    private val context: Context by inject()

    actual fun getDefaultDownloadPath(): String = Environment.DIRECTORY_PICTURES

    actual fun createDirectoryIfAbsent(path: String): Boolean {
        val dir =
            if (path.startsWith("/")) {
                File(path)
            } else {
                val root =
                    path.substringBefore("/").let { segment ->
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
        if (bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Wallpaper image is empty."))
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToMediaStore(path, filename, bytes)
        } else {
            saveToAppExternalStorage(path, filename, bytes)
        }
    }

    actual fun readFile(path: String): Result<ByteArray> = runCatching {
        if (path.isContentUri()) {
            context.contentResolver.openInputStream(Uri.parse(path))?.use { input ->
                input.readBytes()
            } ?: error("Wallpaper file could not be opened.")
        } else {
            File(path).readBytes()
        }
    }

    actual fun fileExists(path: String): Boolean =
        if (path.isContentUri()) {
            runCatching {
                context.contentResolver.query(
                    Uri.parse(path),
                    arrayOf(MediaStore.MediaColumns._ID),
                    null,
                    null,
                    null,
                )?.use { cursor -> cursor.moveToFirst() } == true
            }.getOrDefault(false)
        } else {
            File(path).exists()
        }

    actual fun listFiles(path: String): List<String> =
        File(path).listFiles()?.map { it.absolutePath } ?: emptyList()

    actual fun deleteFile(path: String): Boolean =
        if (path.isContentUri()) {
            runCatching { context.contentResolver.delete(Uri.parse(path), null, null) > 0 }
                .getOrDefault(false)
        } else {
            val file = File(path)
            val deleted = file.delete()
            if (deleted) {
                runCatching {
                    context.contentResolver.delete(
                        MediaStore.Files.getContentUri("external"),
                        "${MediaStore.MediaColumns.DATA} = ?",
                        arrayOf(path),
                    )
                }
            }
            deleted
        }

    actual fun hasWriteAccess(path: String): Boolean = true

    private fun saveToMediaStore(
        path: String,
        filename: String,
        bytes: ByteArray,
    ): Result<String> = runCatching {
        val resolver = context.contentResolver
        val values =
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType(filename))
                put(MediaStore.Images.Media.RELATIVE_PATH, relativeMediaPath(path))
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        val uri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Android MediaStore rejected the wallpaper file.")

        try {
            resolver.openOutputStream(uri)?.use { output -> output.write(bytes) }
                ?: error("Wallpaper file could not be written.")
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri.toString()
        } catch (throwable: Throwable) {
            resolver.delete(uri, null, null)
            throw throwable
        }
    }

    private fun saveToAppExternalStorage(
        path: String,
        filename: String,
        bytes: ByteArray,
    ): Result<String> = runCatching {
        val targetDirectory =
            File(
                context.getExternalFilesDir(mediaDirectory(path)),
                relativeChildPath(path).joinToString(File.separator),
            )
        targetDirectory.mkdirs()
        val target = File(targetDirectory, filename)
        target.writeBytes(bytes)
        target.absolutePath
    }

    private fun relativeMediaPath(path: String): String {
        val segments = relativeChildPath(path)
        return (listOf(mediaDirectory(path), "Scapes") + segments)
            .filter { segment -> segment.isNotBlank() }
            .joinToString("/")
    }

    private fun relativeChildPath(path: String): List<String> {
        val mediaRoots =
            setOf(
                Environment.DIRECTORY_DCIM.lowercase(),
                Environment.DIRECTORY_DOWNLOADS.lowercase(),
                Environment.DIRECTORY_PICTURES.lowercase(),
                "downloads",
                "pictures",
            )

        return path
            .replace('\\', '/')
            .split('/')
            .map { segment -> segment.trim() }
            .filter { segment -> segment.isNotBlank() }
            .dropWhile { segment -> segment.lowercase() in mediaRoots }
            .filterNot { segment -> segment.equals("Scapes", ignoreCase = true) }
    }

    private fun mediaDirectory(path: String): String =
        when {
            path.contains(Environment.DIRECTORY_DCIM, ignoreCase = true) -> Environment.DIRECTORY_DCIM
            path.contains(Environment.DIRECTORY_DOWNLOADS, ignoreCase = true) -> Environment.DIRECTORY_DOWNLOADS
            else -> Environment.DIRECTORY_PICTURES
        }

    private fun mimeType(filename: String): String =
        when (filename.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }

    private fun String.isContentUri(): Boolean = startsWith("content://")
}
