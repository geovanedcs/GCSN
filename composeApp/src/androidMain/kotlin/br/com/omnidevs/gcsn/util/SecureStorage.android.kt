package br.com.omnidevs.gcsn.util

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import com.russhwolf.settings.SharedPreferencesSettings
import androidx.core.content.edit

actual class SecureStorageProvider(private val context: Context) {
    actual fun getSecureSettings(): Settings {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val sharedPreferences = EncryptedSharedPreferences.create(
                context,
                "gcsn_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // Create a wrapper around SharedPreferencesSettings to ensure commit() is used
            return object : Settings by SharedPreferencesSettings(sharedPreferences) {
                override fun putString(key: String, value: String) {
                    Log.d("SecureStorage", "Saving string data for key: $key")
                    sharedPreferences.edit(commit = true) { putString(key, value) }
                }

                override fun getString(key: String, defaultValue: String): String {
                    val value = sharedPreferences.getString(key, defaultValue) ?: defaultValue
                    Log.d("SecureStorage", "Retrieved string data for key: $key (exists: ${value != defaultValue})")
                    return value
                }
            }
        } catch (e: Exception) {
            Log.e("SecureStorage", "Failed to create secure storage", e)
            throw e
        }
    }
}