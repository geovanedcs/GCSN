package br.com.omnidevs.gcsn.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual object ImageCompressor {
    actual suspend fun compressImage(imageBytes: ByteArray, maxSizeKB: Int): ByteArray {
        val originalBitmap = withContext(Dispatchers.IO) {
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        var quality = 90
        var compressedBytes: ByteArray

        do {
            val outputStream = ByteArrayOutputStream()
            withContext(Dispatchers.IO) {
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            compressedBytes = outputStream.toByteArray()
            outputStream.close()

            quality -= 10
        } while (compressedBytes.size > maxSizeKB * 1024 && quality > 10)

        return compressedBytes
    }
}