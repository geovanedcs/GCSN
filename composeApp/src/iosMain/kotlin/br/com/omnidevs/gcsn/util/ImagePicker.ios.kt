package br.com.omnidevs.gcsn.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.lastPathComponent
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeImage
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class ImagePicker {
    private val MAX_IMAGES = 4
    private val MAX_SIZE_BYTES = 1024 * 1024 // 1MB

    @OptIn(ExperimentalForeignApi::class)
    fun processUrlToImageFile(url: NSURL): ImageFile? {
        val path = url.path ?: return null
        val fileManager = NSFileManager.defaultManager

        if (!fileManager.fileExistsAtPath(path)) return null

        val attributes = fileManager.attributesOfItemAtPath(path, null)
        val sizeInBytes = (attributes?.get("NSFileSize") as? NSNumber)?.longValue ?: 0L
        val fileName = url.lastPathComponent ?: "unknown"
        val mimeType = UTType.typeWithFilenameExtension(
            fileName.substringAfterLast('.', "")
        )?.preferredMIMEType ?: "image/jpeg"

        if (sizeInBytes > MAX_SIZE_BYTES) return null

        return ImageFile(
            uri = path,
            name = fileName,
            mimeType = mimeType,
            size = sizeInBytes
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickImages(): List<ImageFile> {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return emptyList()

        val imagePicker = UIImagePickerController()
        imagePicker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        imagePicker.mediaTypes = listOf(UTTypeImage.identifier)

        return suspendCancellableCoroutine { cont ->
            val selectedImages = mutableListOf<ImageFile>()

            val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    val imageUrl =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                    if (imageUrl != null) {
                        processUrlToImageFile(imageUrl)?.let { imageFile ->
                            if (selectedImages.size < MAX_IMAGES) {
                                selectedImages.add(imageFile)
                            }
                        }
                    }
                    rootViewController.dismissViewControllerAnimated(true) {}
                    cont.resume(selectedImages)
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    rootViewController.dismissViewControllerAnimated(true) {}
                    cont.resume(selectedImages)
                }
            }

            imagePicker.delegate = delegate
            rootViewController.presentViewController(imagePicker, true, null)

            cont.invokeOnCancellation {
                rootViewController.dismissViewControllerAnimated(true) {}
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickSingleImage(): ImageFile? {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: return null

        val imagePicker = UIImagePickerController()
        imagePicker.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        imagePicker.mediaTypes = listOf(UTTypeImage.identifier)

        return suspendCancellableCoroutine { cont ->
            val delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    val imageUrl =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                    val imageFile = imageUrl?.let { processUrlToImageFile(it) }
                    rootViewController.dismissViewControllerAnimated(true) {}
                    cont.resume(imageFile)
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    rootViewController.dismissViewControllerAnimated(true) {}
                    cont.resume(null)
                }
            }

            imagePicker.delegate = delegate
            rootViewController.presentViewController(imagePicker, true, null)

            cont.invokeOnCancellation {
                rootViewController.dismissViewControllerAnimated(true) {}
            }
        }
    }
}