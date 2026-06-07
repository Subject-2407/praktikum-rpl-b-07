package com.scapes.platform

/**
 * Desktop encrypted storage placeholder.
 */
actual class EncryptedStorage {
    /**
     * Persists [value] under [key].
     */
    actual fun putString(key: String, value: String) {
        throw UnsupportedOperationException(
            "Desktop encrypted storage requires Windows DPAPI wiring.",
        )
    }

    /**
     * Loads a nullable string stored under [key].
     */
    actual fun getString(key: String): String? {
        throw UnsupportedOperationException(
            "Desktop encrypted storage requires Windows DPAPI wiring.",
        )
    }

    /**
     * Removes a value stored under [key].
     */
    actual fun remove(key: String) {
        throw UnsupportedOperationException(
            "Desktop encrypted storage requires Windows DPAPI wiring.",
        )
    }
}
