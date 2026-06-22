package com.scapes.platform

import java.util.concurrent.ConcurrentHashMap

/**
 * Side-channel registry to pass wallpaper URLs from UI to FileSystemProvider.
 * Handles the 20-character truncation.
 */
object AndroidDownloadRegistry {
    private val urlMap = ConcurrentHashMap<String, String>()

    fun registerUrl(wallpaperId: String, url: String) {
        urlMap[wallpaperId] = url
    }

    fun consumeUrl(filename: String): String? {
        val base = filename.substringBeforeLast(".")
        val extractedId = base.substringAfterLast("-")

        // Try exact match first
        urlMap.remove(extractedId)?.let { return it }

        // Fuzzy suffix-match fallback if repo truncated the ID
        val matchedKey = urlMap.keys.firstOrNull { fullId ->
            fullId.endsWith(extractedId) || extractedId.endsWith(fullId.takeLast(12))
        }

        return matchedKey?.let { urlMap.remove(it) }
    }
}
