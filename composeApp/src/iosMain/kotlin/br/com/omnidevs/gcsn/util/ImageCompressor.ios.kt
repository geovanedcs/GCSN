package br.com.omnidevs.gcsn.util

import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.posix.memcpy

actual object ImageCompressor {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun compressImage(imageBytes: ByteArray, maxSizeKB: Int): ByteArray {
        val nsData = memScoped {
            val bytes = allocArrayOf(imageBytes)
            NSData.dataWithBytes(bytes, imageBytes.size.toULong())
        }

        val image = UIImage.imageWithData(nsData)
            ?: return imageBytes // Return original if we can't decode

        var quality = 0.9
        var compressedBytes = imageBytes // Default to original

        do {
            val compressedData = UIImageJPEGRepresentation(image, quality)
                ?: return imageBytes // Return original if compression fails

            // Convert NSData to ByteArray using usePinned
            val length = compressedData.length.toInt()
            compressedBytes = ByteArray(length)

            compressedBytes.usePinned { pinned ->
                memcpy(
                    pinned.addressOf(0),
                    compressedData.bytes,
                    length.toULong()
                )
            }

            quality -= 0.1
        } while (compressedBytes.size > maxSizeKB * 1024 && quality > 0.1)

        return compressedBytes
    }
}