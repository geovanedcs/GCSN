package br.com.omnidevs.gcsn.util

import kotlinx.cinterop.*
import platform.UIKit.*
import platform.Foundation.*
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.darwin.NSObject
import kotlin.coroutines.resume
import platform.Photos.*

actual class ImagePicker {

    private val viewController: UIViewController

    actual constructor() {
        val keyWindow = UIApplication.sharedApplication.keyWindow
        this.viewController = keyWindow?.rootViewController ?:
                throw IllegalStateException("Não foi possível obter o UIViewController principal")
    }

    constructor(viewController: UIViewController) {
        this.viewController = viewController
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickImages(): List<ImageFile> = suspendCancellableCoroutine { continuation ->
        val imagePickerController = UIImagePickerController()
        imagePickerController.sourceType =
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        imagePickerController.allowsEditing = false
        imagePickerController.mediaTypes =
            listOf("public.image")

        imagePickerController.delegate =
            object : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    // Usando API moderna - UIImagePickerControllerImageURL
                    val imageUrl =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                    val imagePath = imageUrl?.path

                    val selectedImage = if (imagePath != null) {
                        val fileManager = NSFileManager.defaultManager
                        // Safe call para evitar crash
                        val attributes = try {
                            fileManager.attributesOfItemAtPath(imagePath, null) as? Map<Any?, *>
                        } catch (e: Exception) {
                            null
                        }

                        val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue() ?: 0L
                        val fileName = imagePath.split("/").lastOrNull() ?: "imagem.jpg"

                        ImageFile(
                            uri = imagePath,
                            name = fileName,
                            mimeType = "image/jpeg",
                            size = fileSize
                        )
                    } else null

                    viewController.dismissViewControllerAnimated(true) {}
                    if (selectedImage != null) {
                        continuation.resume(listOf(selectedImage))
                    } else {
                        continuation.resume(emptyList())
                    }
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    viewController.dismissViewControllerAnimated(true) {}
                    continuation.resume(emptyList())
                }
            }

        // Verificação de permissões
        val status = PHPhotoLibrary.authorizationStatus()
        when (status) {
            PHAuthorizationStatusAuthorized -> {
                viewController.presentViewController(imagePickerController, true, null)
            }
            else -> {
                PHPhotoLibrary.requestAuthorization { newStatus ->
                    if (newStatus == PHAuthorizationStatusAuthorized) {
                        NSOperationQueue.mainQueue.addOperationWithBlock {
                            viewController.presentViewController(imagePickerController, true, null)
                        }
                    } else {
                        continuation.resume(emptyList())
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun pickSingleImage(): ImageFile? = suspendCancellableCoroutine { continuation ->
        val imagePickerController = UIImagePickerController()
        imagePickerController.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        imagePickerController.allowsEditing = false
        imagePickerController.mediaTypes = listOf("public.image")

        imagePickerController.delegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                // Usando API moderna - UIImagePickerControllerImageURL
                val imageUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
                val imagePath = imageUrl?.path

                val selectedImage = if (imagePath != null) {
                    val fileManager = NSFileManager.defaultManager
                    // Safe call para evitar crash
                    val attributes = try {
                        fileManager.attributesOfItemAtPath(imagePath, null) as? Map<Any?, *>
                    } catch (e: Exception) {
                        null
                    }

                    val fileSize = (attributes?.get(NSFileSize) as? NSNumber)?.longValue() ?: 0L
                    val fileName = imagePath.split("/").lastOrNull() ?: "imagem.jpg"

                    ImageFile(
                        uri = imagePath,
                        name = fileName,
                        mimeType = "image/jpeg",
                        size = fileSize
                    )
                } else null

                viewController.dismissViewControllerAnimated(true) {}
                continuation.resume(selectedImage)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                viewController.dismissViewControllerAnimated(true) {}
                continuation.resume(null)
            }
        }

        // Verificação de permissões
        val status = PHPhotoLibrary.authorizationStatus()
        when (status) {
            PHAuthorizationStatusAuthorized -> {
                viewController.presentViewController(imagePickerController, true, null)
            }
            else -> {
                PHPhotoLibrary.requestAuthorization { newStatus ->
                    if (newStatus == PHAuthorizationStatusAuthorized) {
                        NSOperationQueue.mainQueue.addOperationWithBlock {
                            viewController.presentViewController(imagePickerController, true, null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
            }
        }
    }
}