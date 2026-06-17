package com.scapes.platform

/** Platform file-system operations used by download flows. */
expect class FileSystemProvider {
    /** Returns the default wallpaper download folder. */
    fun getDefaultDownloadPath(): String

    /** Creates [path] when it does not already exist. */
    fun createDirectoryIfAbsent(path: String): Boolean

    /** Saves [bytes] as [filename] inside [path]. */
    fun saveFile(path: String, filename: String, bytes: ByteArray): Result<String>

    /** Reads raw bytes from a saved file. */
    fun readFile(path: String): Result<ByteArray>

    /** Returns whether [path] currently exists as a file. */
    fun fileExists(path: String): Boolean

    /** Lists files inside [path]. */
    fun listFiles(path: String): List<String>

    /** Deletes the file at [path]. */
    fun deleteFile(path: String): Boolean

    /** Returns whether [path] is writable by the app. */
    fun hasWriteAccess(path: String): Boolean
}
