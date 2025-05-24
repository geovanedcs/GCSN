package br.com.omnidevs.gcsn.util

import com.russhwolf.settings.ExperimentalSettingsImplementation
import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings

actual class SecureStorageProvider {
    @OptIn(ExperimentalSettingsImplementation::class)
    actual fun getSecureSettings(): Settings {
        return KeychainSettings(
            service = "br.com.omnidevs.gcsn"
        )
    }
}