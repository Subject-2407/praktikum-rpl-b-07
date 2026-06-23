package com.scapes.platform

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val PreferencesStorageName = "scapes_preferences"
private const val LegacySettingsStorageName = "scapes_settings"
private const val LegacySecureStorageName = "scapes_secure_storage"

/** Android preferences storage backed by unencrypted SharedPreferences. */
actual class PreferencesStorage : KoinComponent {
    private val context: Context by inject()
    private val prefs by lazy {
        context.getSharedPreferences(PreferencesStorageName, Context.MODE_PRIVATE)
    }
    private val legacySettingsPrefs by lazy {
        context.getSharedPreferences(LegacySettingsStorageName, Context.MODE_PRIVATE)
    }
    private val legacySecurePrefs by lazy {
        context.getSharedPreferences(LegacySecureStorageName, Context.MODE_PRIVATE)
    }

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        legacySettingsPrefs.edit().remove(key).apply()
        legacySecurePrefs.edit().remove(key).apply()
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? =
        prefs.getString(key, null)
            ?: legacySettingsPrefs.getString(key, null)
            ?: legacySecurePrefs.getString(key, null)

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
        legacySettingsPrefs.edit().remove(key).apply()
        legacySecurePrefs.edit().remove(key).apply()
    }
}
