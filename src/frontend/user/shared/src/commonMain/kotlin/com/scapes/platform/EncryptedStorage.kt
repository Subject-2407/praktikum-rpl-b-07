package com.scapes.platform

/** Platform-secure key-value storage for API keys. */
expect class EncryptedStorage() {
    /** Persists [value] under [key]. */
    fun putString(key: String, value: String)

    /** Loads a nullable string stored under [key]. */
    fun getString(key: String): String?

    /** Removes a value stored under [key]. */
    fun remove(key: String)
}
