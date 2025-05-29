package br.com.omnidevs.gcsn.util

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import kotlinx.coroutines.launch

@Composable
actual fun getImagePickerManager(): ImagePickerManager {
    return remember { ImagePickerManager() }
}

@Composable
fun rememberImagePickerManager(): ImagePickerManager {
    return remember { ImagePickerManager() }
}

@Composable
fun ImagePickerLauncher(
    manager: ImagePickerManager,
    onResult: (() -> Unit)? = null
): () -> Unit {
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        coroutineScope.launch {
            manager.addImages(uris)
            onResult?.invoke()
        }
    }
    return { launcher.launch("image/*") }
}