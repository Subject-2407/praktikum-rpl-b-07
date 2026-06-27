package com.scapes.platform

import com.sun.jna.platform.win32.Crypt32Util
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Base64
import java.util.Properties

private const val SecureStorageFileName = "api-keys.properties"

/** Desktop encrypted storage backed by Windows DPAPI user-scoped encryption. */
actual class EncryptedStorage {
    private val storagePath = appDataDirectory("secure").resolve(SecureStorageFileName)

    /** Persists [value] under [key]. */
    actual fun putString(key: String, value: String) =
        synchronized(this) {
            requireStorageKey(key)
            val properties = loadProperties()
            val protectedBytes =
                Crypt32Util.cryptProtectData(value.toByteArray(StandardCharsets.UTF_8))
            properties.setProperty(key, Base64.getEncoder().encodeToString(protectedBytes))
            storeProperties(properties)
        }

    /** Loads a nullable string stored under [key]. */
    actual fun getString(key: String): String? =
        synchronized(this) {
            requireStorageKey(key)
            val encoded = loadProperties().getProperty(key) ?: return@synchronized null
            val protectedBytes = Base64.getDecoder().decode(encoded)
            val rawBytes = Crypt32Util.cryptUnprotectData(protectedBytes)
            rawBytes.toString(StandardCharsets.UTF_8)
        }

    /** Removes a value stored under [key]. */
    actual fun remove(key: String) =
        synchronized(this) {
            requireStorageKey(key)
            val properties = loadProperties()
            if (properties.remove(key) != null) {
                storeProperties(properties)
            }
        }

    private fun loadProperties(): Properties {
        val properties = Properties()
        if (Files.exists(storagePath)) {
            Files.newInputStream(storagePath).use(properties::load)
        }
        return properties
    }

    private fun storeProperties(properties: Properties) {
        Files.createDirectories(storagePath.parent)
        Files.newOutputStream(storagePath).use { output ->
            properties.store(output, "Scapes secure desktop storage")
        }
    }

    private fun requireStorageKey(key: String) {
        require(key.matches(Regex("[A-Za-z0-9_.-]+"))) { "Invalid storage key." }
    }
}
