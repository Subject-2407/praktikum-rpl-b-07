package com.scapes.platform

/** Non-sensitive platform key-value storage for app preferences. */
expect class PreferencesStorage() {
    /** Persists [value] under [key]. */
    fun putString(key: String, value: String)

    /** Loads a nullable string stored under [key]. */
    fun getString(key: String): String?

    /** Removes the value stored under [key]. */
    fun remove(key: String)
}
