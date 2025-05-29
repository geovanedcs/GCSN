package br.com.omnidevs.gcsn.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

actual class ImagePickerManager {
    private val imagePicker: ImagePicker = ImagePicker()
    private val _selectedImages = MutableStateFlow<List<ImageFile>>(emptyList())
    actual val selectedImages: StateFlow<List<ImageFile>> = _selectedImages.asStateFlow()

    private val _selectedImage = MutableStateFlow<ImageFile?>(null)
    actual val selectedImage: StateFlow<ImageFile?> = _selectedImage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    actual val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private val MAX_IMAGES = 4

    actual constructor()

    actual suspend fun pickImages() {
        _errorMessage.value = null
        try {
            val images = withContext(Dispatchers.Main) { imagePicker.pickImages() }
            val availableSlots = MAX_IMAGES - _selectedImages.value.size
            val imagesToAdd = images.take(availableSlots)
            _selectedImages.value = _selectedImages.value + imagesToAdd
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao selecionar imagens: ${e.message}"
        }
    }

    actual suspend fun pickSingleImage() {
        _errorMessage.value = null
        try {
            val image = withContext(Dispatchers.Main) { imagePicker.pickSingleImage() }
            _selectedImage.value = image
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao selecionar imagem: ${e.message}"
        }
    }

    actual fun removeImage(imageFile: ImageFile) {
        val currentList = _selectedImages.value.toMutableList()
        currentList.remove(imageFile)
        _selectedImages.value = currentList
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getImageBytes(imageUri: String): ByteArray {
        return withContext(Dispatchers.Default) {
            val url = NSURL(string = imageUri)
            val data = NSData.dataWithContentsOfURL(url)
                ?: throw IllegalArgumentException("Não foi possível ler os dados da imagem: $imageUri")
            ByteArray(data.length.toInt()).apply {
                usePinned {
                    memcpy(it.addressOf(0), data.bytes, data.length)
                }
            }
        }
    }

    actual suspend fun addImages(uris: List<Any>) {
        _errorMessage.value = null
        val nsUrls = uris.filterIsInstance<NSURL>()
        if (nsUrls.isEmpty()) return

        val availableSlots = MAX_IMAGES - _selectedImages.value.size
        if (availableSlots <= 0) {
            _errorMessage.value = "Máximo de $MAX_IMAGES imagens permitidas"
            return
        }

        try {
            val imagesToAdd = nsUrls.take(availableSlots).mapNotNull { url ->
                withContext(Dispatchers.Main) {
                    imagePicker.processUrlToImageFile(url)
                }
            }
            _selectedImages.value = _selectedImages.value + imagesToAdd
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao adicionar imagens: ${e.message}"
        }
    }
}