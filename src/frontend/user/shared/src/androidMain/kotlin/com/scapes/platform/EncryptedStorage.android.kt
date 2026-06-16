package com.scapes.platform

/** Android encrypted storage placeholder. */
actual class EncryptedStorage {
    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        throw UnsupportedOperationException("Android encrypted storage requires Context wiring.")
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? {
        throw UnsupportedOperationException("Android encrypted storage requires Context wiring.")
    }

    /** Removes a value stored under [key]. */
    actual fun remove(key: String) {
        throw UnsupportedOperationException("Android encrypted storage requires Context wiring.")
    }
}
