package com.scapes.domain.model

/**
 * Search query normalization rules.
 */
object SearchQuery {
    /** Maximum provider-safe query length. */
    const val MAX_LENGTH = 100

    /**
     * Returns a trimmed and bounded query, or null when [raw] is blank.
     *
     * @param raw user-entered search text.
     */
    fun normalize(raw: String): String? {
        val trimmed = raw.trim()

        if (trimmed.isEmpty()) {
            return null
        }

        return trimmed.take(MAX_LENGTH)
    }
}
