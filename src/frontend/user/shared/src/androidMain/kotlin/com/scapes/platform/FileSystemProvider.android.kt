package com.scapes.platform

/**
 * Android file-system placeholder.
 */
actual class FileSystemProvider {
    /**
     * Returns the default wallpaper download folder.
     */
    actual fun getDefaultDownloadPath(): String {
        throw UnsupportedOperationException("Android file-system provider requires Context wiring.")
    }

    /**
     * Creates [path] when it does not already exist.
     */
    actual fun createDirectoryIfAbsent(path: String): Boolean {
        throw UnsupportedOperationException("Android file-system provider requires Context wiring.")
    }

    /**
     * Saves [bytes] as [filename] inside [path].
     */
    actual fun saveFile(
        path: String,
        filename: String,
        bytes: ByteArray,
    ): Result<String> =
        Result.failure(
            UnsupportedOperationException("Android file-system provider requires Context wiring."),
        )

    /**
     * Lists files inside [path].
     */
    actual fun listFiles(path: String): List<String> {
        throw UnsupportedOperationException("Android file-system provider requires Context wiring.")
    }

    /**
     * Deletes the file at [path].
     */
    actual fun deleteFile(path: String): Boolean {
        throw UnsupportedOperationException("Android file-system provider requires Context wiring.")
    }

    /**
     * Returns whether [path] is writable by the app.
     */
    actual fun hasWriteAccess(path: String): Boolean {
        throw UnsupportedOperationException("Android file-system provider requires Context wiring.")
    }
}
