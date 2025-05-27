package br.com.omnidevs.gcsn.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresExtension
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class ImagePicker {

    private lateinit var activity: Activity

    actual constructor() {
        throw IllegalStateException("É necessário fornecer uma Activity para o ImagePicker")
    }

    constructor(activity: Activity) {
        this.activity = activity
    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
    actual suspend fun pickImages(): List<ImageFile> = suspendCancellableCoroutine { continuation ->
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES).apply {
            putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10)
        }

        // Cria um ActivityResultListener temporário
        val resultCallback = object : ActivityResultCallback {
            override fun onActivityResult(resultCode: Int, data: Intent?) {
                activity.removeActivityResultListener(this)

                if (resultCode == Activity.RESULT_OK && data != null) {
                    val selectedImages = mutableListOf<ImageFile>()

                    // Processar múltiplas imagens
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            val imageFile = getImageFileFromUri(uri)
                            if (imageFile != null) {
                                selectedImages.add(imageFile)
                            }
                        }
                    } else if (data.data != null) {
                        // Processar uma única imagem
                        val imageFile = getImageFileFromUri(data.data!!)
                        if (imageFile != null) {
                            selectedImages.add(imageFile)
                        }
                    }

                    continuation.resume(selectedImages)
                } else {
                    continuation.resume(emptyList())
                }
            }
        }

        // Adiciona o listener e inicia a activity
        activity.addActivityResultListener(resultCallback)
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    actual suspend fun pickSingleImage(): ImageFile? = suspendCancellableCoroutine { continuation ->
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }

        // Cria um ActivityResultListener temporário
        val resultCallback = object : ActivityResultCallback {
            override fun onActivityResult(resultCode: Int, data: Intent?) {
                activity.removeActivityResultListener(this)

                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    val imageFile = getImageFileFromUri(data.data!!)
                    continuation.resume(imageFile)
                } else {
                    continuation.resume(null)
                }
            }
        }

        // Adiciona o listener e inicia a activity
        activity.addActivityResultListener(resultCallback)
        activity.startActivityForResult(intent, REQUEST_SINGLE_IMAGE_PICK)
    }

    private fun getImageFileFromUri(uri: Uri): ImageFile? {
        // Código mantido como estava
        val cursor = activity.contentResolver.query(
            uri,
            arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE
            ),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeIndex = it.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
                val sizeIndex = it.getColumnIndex(MediaStore.Images.Media.SIZE)

                val name =
                    if (nameIndex != -1) it.getString(nameIndex) else "image_${System.currentTimeMillis()}"
                val mimeType = if (mimeIndex != -1) it.getString(mimeIndex) else "image/jpeg"
                val size = if (sizeIndex != -1) it.getLong(sizeIndex) else 0

                return ImageFile(uri.toString(), name, mimeType, size)
            }
        }

        return null
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_SINGLE_IMAGE_PICK = 101
    }
}

// Interface para o callback de resultado da Activity
interface ActivityResultCallback {
    fun onActivityResult(resultCode: Int, data: Intent?)
}

// Extensões para gerenciar callbacks de resultado de Activity
private val activityResultListeners =
    mutableMapOf<Activity, MutableMap<ActivityResultCallback, Int>>()

fun Activity.addActivityResultListener(callback: ActivityResultCallback): Int {
    if (activityResultListeners[this] == null) {
        activityResultListeners[this] = mutableMapOf()
    }
    val requestCode = callback.hashCode() and 0xFFFF
    activityResultListeners[this]!![callback] = requestCode
    return requestCode
}

fun Activity.removeActivityResultListener(callback: ActivityResultCallback) {
    activityResultListeners[this]?.remove(callback)
}

