package br.com.omnidevs.gcsn.util

import androidx.compose.runtime.Composable

@Composable
actual fun getImagePickerManager(): ImagePickerManager {
    return ImagePickerManager()
}