package com.scapes.platform

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val PreferencesStorageName = "scapes_preferences"
private const val LegacyPreferencesStorageName = "scapes_secure_storage"

/** Android preferences storage backed by unencrypted SharedPreferences. */
actual class PreferencesStorage : KoinComponent {
    private val context: Context by inject()
    private val legacyPrefs by lazy {
        context.getSharedPreferences(LegacyPreferencesStorageName, Context.MODE_PRIVATE)
    }
    private val prefs by lazy {
        context.getSharedPreferences(PreferencesStorageName, Context.MODE_PRIVATE)
    }

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        legacyPrefs.edit().remove(key).apply()
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? =
        prefs.getString(key, null) ?: legacyPrefs.getString(key, null)

    /** Removes the value stored under [key]. */
    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
        legacyPrefs.edit().remove(key).apply()
    }
}
