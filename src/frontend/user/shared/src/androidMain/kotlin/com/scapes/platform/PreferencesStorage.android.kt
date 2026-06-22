package com.scapes.platform

/** Android preferences storage placeholder. */
actual class PreferencesStorage {
    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        throw UnsupportedOperationException("Android preferences storage requires Context wiring.")
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? {
        throw UnsupportedOperationException("Android preferences storage requires Context wiring.")
    }

    /** Removes the value stored under [key]. */
    actual fun remove(key: String) {
        throw UnsupportedOperationException("Android preferences storage requires Context wiring.")
    }
}
