package com.scapes.platform

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

actual class PreferencesStorage : KoinComponent {
    private val context: Context by inject()
    private val prefs by lazy {
        context.getSharedPreferences("scapes_settings", Context.MODE_PRIVATE)
    }

    init {
        // Default organization = NONE
        if (prefs.getString("download_organization", null) == null) {
            prefs.edit().putString("download_organization", "NONE").apply()
        }
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
