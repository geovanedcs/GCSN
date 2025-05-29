package br.com.omnidevs.gcsn.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
class IosMediaContentReader : MediaContentReader {
    override suspend fun getMediaBytes(filePath: String): ByteArray {
        val nsData = NSData.dataWithContentsOfFile(filePath)
            ?: throw IllegalStateException("Não foi possível ler o arquivo: $filePath")

        val length = nsData.length.toInt()
        val byteArray = ByteArray(length)

        byteArray.usePinned { pinnedData ->
            memcpy(
                pinnedData.addressOf(0),
                nsData.bytes,
                length.toULong()
            )
        }

        return byteArray
    }
}