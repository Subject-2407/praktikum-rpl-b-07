package com.scapes.platform

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Android encrypted storage using SharedPreferences. */
actual class EncryptedStorage : KoinComponent {
    private val context: Context by inject()
    private val prefs by lazy {
        context.getSharedPreferences("scapes_secure_storage", Context.MODE_PRIVATE)
    }

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    /** Removes a value stored under [key]. */
    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
