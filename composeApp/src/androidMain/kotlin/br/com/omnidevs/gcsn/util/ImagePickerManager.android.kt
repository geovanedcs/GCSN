package br.com.omnidevs.gcsn.util

    import android.app.Activity
    import android.os.Build
    import androidx.annotation.RequiresExtension
    import androidx.compose.ui.platform.LocalContext
    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.launch

    actual class ImagePickerManager {
        private val imagePicker: ImagePicker

        private val _selectedImages = MutableStateFlow<List<ImageFile>>(emptyList())
        actual val selectedImages: StateFlow<List<ImageFile>> = _selectedImages.asStateFlow()

        private val _selectedImage = MutableStateFlow<ImageFile?>(null)
        actual val selectedImage: StateFlow<ImageFile?> = _selectedImage.asStateFlow()

        private val scope = CoroutineScope(Dispatchers.Main)

        // Construtor sem parâmetros que corresponde ao expect
        actual constructor() {
            // Utilizará a Activity atual através do applicationContext global
            // Nota: Isso requer que a Activity principal seja inicializada antes
            val currentActivity = ApplicationContext.activity
                ?: throw IllegalStateException("Activity não inicializada. Chame ApplicationContext.initialize na sua MainActivity")

            this.imagePicker = ImagePicker(currentActivity)
        }

        // Construtor secundário que aceita uma Activity específica
        constructor(activity: Activity) {
            this.imagePicker = ImagePicker(activity)
        }

        @RequiresExtension(extension = Build.VERSION_CODES.R, version = 2)
        actual fun pickImages() {
            scope.launch {
                val images = imagePicker.pickImages()
                _selectedImages.value = images
            }
        }

        actual fun pickSingleImage() {
            scope.launch {
                val image = imagePicker.pickSingleImage()
                _selectedImage.value = image
            }
        }

        actual fun removeImage(imageFile: ImageFile) {
            val currentList = _selectedImages.value.toMutableList()
            currentList.remove(imageFile)
            _selectedImages.value = currentList
        }
    }