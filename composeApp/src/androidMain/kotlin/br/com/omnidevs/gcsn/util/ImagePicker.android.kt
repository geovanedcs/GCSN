package br.com.omnidevs.gcsn.util

import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

actual class ImagePicker {

    // Constants
    private val MAX_IMAGE_SIZE_BYTES = 1024 * 1024 // 1MB
    private val MAX_IMAGES_PER_POST = 4

    // Launchers
    private var pickImagesLauncher: ActivityResultLauncher<String>? = null
    private var pickSingleImageLauncher: ActivityResultLauncher<String>? = null

    // Continuations
    private var pickImagesContinuation: ((List<Uri>) -> Unit)? = null
    private var pickSingleImageContinuation: ((Uri?) -> Unit)? = null

    actual constructor()

    private fun ensureLaunchers() {
        val activity = ApplicationContext.activity as? ComponentActivity ?: return
        if (pickImagesLauncher == null) {
            pickImagesLauncher = activity.registerForActivityResult(
                ActivityResultContracts.GetMultipleContents()
            ) { uris ->
                pickImagesContinuation?.invoke(uris)
                pickImagesContinuation = null
            }
        }
        if (pickSingleImageLauncher == null) {
            pickSingleImageLauncher = activity.registerForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                pickSingleImageContinuation?.invoke(uri)
                pickSingleImageContinuation = null
            }
        }
    }

    actual suspend fun pickImages(): List<ImageFile> = suspendCancellableCoroutine { continuation ->
        val activity = ApplicationContext.activity as? ComponentActivity
        if (activity == null) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }
        ensureLaunchers()
        pickImagesContinuation = { uris ->
            MainScope().launch {
                val limitedUris = uris.take(MAX_IMAGES_PER_POST)
                val selectedImages = limitedUris.mapNotNull { processUriToImageFile(it) }
                continuation.resume(selectedImages)
            }
        }
        pickImagesLauncher?.launch("image/*")
    }

    actual suspend fun pickSingleImage(): ImageFile? = suspendCancellableCoroutine { continuation ->
        val activity = ApplicationContext.activity as? ComponentActivity
        if (activity == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        ensureLaunchers()
        pickSingleImageContinuation = { uri ->
            MainScope().launch {
                val imageFile = uri?.let { processUriToImageFile(it) }
                continuation.resume(imageFile)
            }
        }
        pickSingleImageLauncher?.launch("image/*")
    }

    private suspend fun processUriToImageFile(uri: Uri): ImageFile? = withContext(Dispatchers.IO) {
        try {
            val activity = ApplicationContext.activity ?: return@withContext null
            val contentResolver = activity.contentResolver
            val fileInfo = getFileInfo(contentResolver, uri)
            val fileName = fileInfo.first ?: "img_${System.currentTimeMillis()}.jpg"
            val mimeType = fileInfo.second ?: contentResolver.getType(uri) ?: "image/jpeg"

            val cacheDir = activity.cacheDir ?: return@withContext null
            val destinationFile = File(cacheDir, "img_${System.currentTimeMillis()}_${fileName}")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            val size = destinationFile.length()
            if (size <= MAX_IMAGE_SIZE_BYTES) {
                ImageFile(
                    uri = destinationFile.toURI().toString(),
                    name = fileName,
                    mimeType = mimeType,
                    size = size
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error processing image", e)
            null
        }
    }

    private fun getFileInfo(
        contentResolver: android.content.ContentResolver,
        uri: Uri
    ): Pair<String?, String?> {
        var fileName: String? = null
        var mimeType: String? = null
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                    val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                    if (mimeTypeIndex != -1) {
                        mimeType = cursor.getString(mimeTypeIndex)
                    }
                }
            }
            if (fileName == null && uri.path != null) {
                fileName = uri.path?.substringAfterLast('/')
            }
            if (mimeType == null) {
                mimeType = contentResolver.getType(uri)
            }
        } catch (e: Exception) {
            Log.e("ImagePicker", "Error getting file info", e)
        }
        return Pair(fileName, mimeType)
    }
}