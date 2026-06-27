package com.scapes.platform

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.isRegularFile

/** Desktop file-system implementation. */
actual class FileSystemProvider {
    /** Returns the default wallpaper download folder. */
    actual fun getDefaultDownloadPath(): String {
        val userHome = System.getProperty("user.home") ?: "."
        return Path.of(userHome, "Pictures", "Scapes").toString()
    }

    /** Creates [path] when it does not already exist. */
    actual fun createDirectoryIfAbsent(path: String): Boolean =
        runCatching {
                Files.createDirectories(Path.of(path))
                true
            }
            .getOrDefault(false)

    /** Saves [bytes] as [filename] inside [path]. */
    actual fun saveFile(path: String, filename: String, bytes: ByteArray): Result<String> =
        runCatching {
            val directory = Path.of(path).normalize()
            Files.createDirectories(directory)

            val target = directory.resolve(filename.safeFilename()).normalize()
            require(target.startsWith(directory)) { "Filename cannot escape the download folder." }

            Files.write(
                target,
                bytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE,
            )
            target.toString()
        }

    /** Reads raw bytes from a saved file. */
    actual fun readFile(path: String): Result<ByteArray> =
        runCatching { Files.readAllBytes(Path.of(path).normalize()) }

    /** Returns whether [path] currently exists as a file. */
    actual fun fileExists(path: String): Boolean =
        runCatching { Path.of(path).isRegularFile() }.getOrDefault(false)

    /** Lists files inside [path]. */
    actual fun listFiles(path: String): List<String> =
        runCatching {
                Files.list(Path.of(path)).use { stream ->
                    stream
                        .filter { file -> file.isRegularFile() }
                        .map { file -> file.toString() }
                        .toList()
                }
            }
            .getOrDefault(emptyList())

    /** Deletes the file at [path]. */
    actual fun deleteFile(path: String): Boolean =
        runCatching { Files.deleteIfExists(Path.of(path)) }.getOrDefault(false)

    /** Returns whether [path] is writable by the app. */
    actual fun hasWriteAccess(path: String): Boolean {
        val candidate = nearestExistingDirectory(Path.of(path).normalize()) ?: return false

        return Files.isDirectory(candidate) && Files.isWritable(candidate)
    }

    private fun nearestExistingDirectory(path: Path): Path? {
        var candidate: Path? = path
        while (candidate != null && !Files.exists(candidate)) {
            candidate = candidate.parent
        }
        return candidate
    }

    private fun String.safeFilename(): String =
        substringAfterLast('/').substringAfterLast('\\').ifBlank { "wallpaper" }
}
