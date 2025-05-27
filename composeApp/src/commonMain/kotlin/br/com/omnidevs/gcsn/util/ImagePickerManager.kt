package br.com.omnidevs.gcsn.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

expect class ImagePickerManager() {
    val selectedImages: StateFlow<List<ImageFile>>
    val selectedImage: StateFlow<ImageFile?>

    fun pickImages()
    fun pickSingleImage()
    fun removeImage(imageFile: ImageFile)
}