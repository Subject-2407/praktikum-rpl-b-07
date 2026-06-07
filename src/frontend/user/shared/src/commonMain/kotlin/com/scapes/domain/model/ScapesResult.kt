package com.scapes.domain.model

/**
 * Typed result used across Scapes layer boundaries.
 */
sealed class ScapesResult<out T> {
    /**
     * Successful operation result.
     *
     * @property data operation payload.
     */
    data class Success<T>(
        val data: T,
    ) : ScapesResult<T>()

    /**
     * Recoverable operation failure.
     *
     * @property code stable error category.
     * @property message human-readable error message.
     */
    data class Error(
        val code: ErrorCode,
        val message: String,
    ) : ScapesResult<Nothing>()

    /**
     * In-flight operation marker for UI state bridges.
     */
    data object Loading : ScapesResult<Nothing>()
}
