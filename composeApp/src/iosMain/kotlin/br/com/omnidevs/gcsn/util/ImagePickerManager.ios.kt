    package br.com.omnidevs.gcsn.util

    import kotlinx.coroutines.CoroutineScope
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.launch
    import platform.UIKit.UIViewController

    actual class ImagePickerManager {
        private val imagePicker: ImagePicker

        private val _selectedImages = MutableStateFlow<List<ImageFile>>(emptyList())
        actual val selectedImages: StateFlow<List<ImageFile>> = _selectedImages.asStateFlow()

        private val _selectedImage = MutableStateFlow<ImageFile?>(null)
        actual val selectedImage: StateFlow<ImageFile?> = _selectedImage.asStateFlow()

        private val scope = CoroutineScope(Dispatchers.Main)

        actual constructor() {
            this.imagePicker = ImagePicker()
        }

        constructor(viewController: UIViewController) {
            this.imagePicker = ImagePicker(viewController)
        }

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