package br.com.omnidevs.gcsn.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import kotlin.compareTo

actual class ImagePickerManager {
    private val imagePicker = ImagePicker()
    private val MAX_IMAGES = 4

    private val _selectedImages = MutableStateFlow<List<ImageFile>>(emptyList())
    actual val selectedImages: StateFlow<List<ImageFile>> = _selectedImages.asStateFlow()

    private val _selectedImage = MutableStateFlow<ImageFile?>(null)
    actual val selectedImage: StateFlow<ImageFile?> = _selectedImage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    actual val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    actual constructor()

    actual suspend fun pickImages() {
        _errorMessage.value = null

        if (_selectedImages.value.size >= MAX_IMAGES) {
            _errorMessage.value = "Máximo de $MAX_IMAGES imagens permitidas"
            return
        }

        try {
            val pickedImages = imagePicker.pickImages()
            if (pickedImages.isEmpty()) return

            val availableSlots = MAX_IMAGES - _selectedImages.value.size
            val imagesToProcess = pickedImages.take(availableSlots)
            val processedImages = createLocalImageCopies(imagesToProcess)
            _selectedImages.update { current -> current + processedImages }
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao selecionar imagens: ${e.message}"
        }
    }

    actual suspend fun pickSingleImage() {
        _errorMessage.value = null
        try {
            val image = imagePicker.pickSingleImage() ?: return
            val processedImage = createLocalImageCopy(image)
            _selectedImage.value = processedImage
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao selecionar imagem: ${e.message}"
        }
    }

    actual fun removeImage(imageFile: ImageFile) {
        _selectedImages.update { current -> current.filterNot { it == imageFile } }
        try {
            val file = File(imageFile.uri.removePrefix("file://"))
            if (file.exists()) file.delete()
        } catch (_: Exception) {
        }
    }

    private suspend fun createLocalImageCopies(images: List<ImageFile>): List<ImageFile> =
        withContext(Dispatchers.IO) {
            images.mapNotNull { createLocalImageCopy(it) }
        }

    private suspend fun createLocalImageCopy(image: ImageFile): ImageFile? =
        withContext(Dispatchers.IO) {
            try {
                val contentResolver =
                    ApplicationContext.activity?.contentResolver ?: return@withContext null
                val uri = image.uri.toUri()
                val cacheDir = ApplicationContext.activity?.cacheDir ?: return@withContext null
                val fileName = "img_${System.currentTimeMillis()}.jpg"
                val file = File(cacheDir, fileName)
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                return@withContext ImageFile(
                    uri = file.toURI().toString(),
                    name = image.name,
                    mimeType = image.mimeType,
                    size = file.length()
                )
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao processar imagem: ${e.message}"
                return@withContext null
            }
        }

    actual suspend fun getImageBytes(imageUri: String): ByteArray =
        withContext(Dispatchers.IO) {
            try {
                if (imageUri.startsWith("file://")) {
                    val file = File(imageUri.removePrefix("file://"))
                    return@withContext file.readBytes()
                }
                val contentResolver = ApplicationContext.activity?.contentResolver
                    ?: throw IllegalStateException("Activity não inicializada")
                val uri = imageUri.toUri()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.readBytes()
                } ?: throw IllegalArgumentException("Não foi possível abrir o URI: $imageUri")
            } catch (e: Exception) {
                throw IllegalStateException("Erro ao ler imagem: ${e.message}", e)
            }
        }
    actual suspend fun addImages(uris: List<Any>) {
        _errorMessage.value = null

        val androidUris = uris.filterIsInstance<android.net.Uri>()
        if (androidUris.isEmpty()) return

        val availableSlots = MAX_IMAGES - _selectedImages.value.size
        if (availableSlots <= 0) {
            _errorMessage.value = "Máximo de $MAX_IMAGES imagens permitidas"
            return
        }

        try {
            val imagesToProcess = androidUris.take(availableSlots)
            val newImages = imagesToProcess.mapNotNull { uri ->
                // Use the same logic as in ImagePicker to convert Uri to ImageFile
                withContext(Dispatchers.IO) {
                    imagePicker.run {
                        val imageFile = this::class.java
                            .getDeclaredMethod("processUriToImageFile", android.net.Uri::class.java)
                            .apply { isAccessible = true }
                            .invoke(this, uri) as? ImageFile
                        imageFile
                    }
                }
            }
            _selectedImages.update { current -> current + newImages }
        } catch (e: Exception) {
            _errorMessage.value = "Erro ao adicionar imagens: ${e.message}"
        }
    }
}