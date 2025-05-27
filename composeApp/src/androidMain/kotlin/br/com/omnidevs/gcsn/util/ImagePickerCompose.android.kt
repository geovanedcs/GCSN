package br.com.omnidevs.gcsn.util

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@SuppressLint("ContextCastToActivity")
@Composable
actual fun getImagePickerManager(): ImagePickerManager {
    val activity = LocalContext.current as android.app.Activity
    return ImagePickerManager(activity)
}