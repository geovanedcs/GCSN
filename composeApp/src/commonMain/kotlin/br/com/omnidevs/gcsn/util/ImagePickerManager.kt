package br.com.omnidevs.gcsn.util

import kotlinx.coroutines.flow.StateFlow

expect class ImagePickerManager() {
    val selectedImages: StateFlow<List<ImageFile>>
    val selectedImage: StateFlow<ImageFile?>

    fun pickImages()
    fun pickSingleImage()
    fun removeImage(imageFile: ImageFile)
    suspend fun getImageBytes(imageUri: String): ByteArray
}