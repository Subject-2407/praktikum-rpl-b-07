package com.scapes.platform

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val LegacySecureStorageName = "scapes_secure_storage"
private const val SecureStorageName = "scapes_encrypted_storage"

/** Android encrypted storage for API keys and legacy secure-storage migration. */
actual class EncryptedStorage : KoinComponent {
    private val context: Context by inject()
    private val legacyPrefs by lazy {
        context.getSharedPreferences(LegacySecureStorageName, Context.MODE_PRIVATE)
    }
    private val prefs by lazy {
        val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        EncryptedSharedPreferences.create(
            context,
            SecureStorageName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        legacyPrefs.edit().remove(key).apply()
    }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? {
        return prefs.getString(key, null) ?: legacyPrefs.getString(key, null)
    }

    /** Removes a value stored under [key]. */
    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
        legacyPrefs.edit().remove(key).apply()
    }
}
