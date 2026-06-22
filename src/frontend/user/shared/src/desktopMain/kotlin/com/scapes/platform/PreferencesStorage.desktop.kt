package com.scapes.platform

import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences

/** Desktop preferences storage backed by Java Preferences. */
actual class PreferencesStorage {
    private val settings by lazy {
        PreferencesSettings(Preferences.userRoot().node("com/scapes/preferences"))
    }

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        settings.putString(key, value)
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? =
        if (settings.hasKey(key)) settings.getString(key, "") else null

    /** Removes the value stored under [key]. */
    actual fun remove(key: String) {
        settings.remove(key)
    }
}
