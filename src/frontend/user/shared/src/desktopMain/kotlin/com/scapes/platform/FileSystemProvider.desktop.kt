package com.scapes.platform

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * Desktop file-system implementation.
 */
actual class FileSystemProvider {
    /**
     * Returns the default wallpaper download folder.
     */
    actual fun getDefaultDownloadPath(): String {
        val userHome = System.getProperty("user.home") ?: "."
        return Path.of(userHome, "Pictures", "Scapes").toString()
    }

    /**
     * Creates [path] when it does not already exist.
     */
    actual fun createDirectoryIfAbsent(path: String): Boolean =
        runCatching {
            Files.createDirectories(Path.of(path))
            true
        }.getOrDefault(false)

    /**
     * Saves [bytes] as [filename] inside [path].
     */
    actual fun saveFile(
        path: String,
        filename: String,
        bytes: ByteArray,
    ): Result<String> =
        runCatching {
            val directory = Path.of(path).normalize()
            Files.createDirectories(directory)

            val target = directory.resolve(filename).normalize()
            require(target.startsWith(directory)) {
                "Filename cannot escape the download folder."
            }

            Files.write(target, bytes)
            target.toString()
        }

    /**
     * Lists files inside [path].
     */
    actual fun listFiles(path: String): List<String> =
        runCatching {
            Files.list(Path.of(path)).use { stream ->
                stream
                    .filter { file -> file.isRegularFile() }
                    .map { file -> file.toString() }
                    .toList()
            }
        }.getOrDefault(emptyList())

    /**
     * Deletes the file at [path].
     */
    actual fun deleteFile(path: String): Boolean =
        runCatching {
            Files.deleteIfExists(Path.of(path))
        }.getOrDefault(false)

    /**
     * Returns whether [path] is writable by the app.
     */
    actual fun hasWriteAccess(path: String): Boolean {
        val folder = Path.of(path)
        val candidate = if (Files.exists(folder)) folder else folder.parent

        return candidate != null && Files.isWritable(candidate)
    }
}
