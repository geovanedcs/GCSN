package br.com.omnidevs.gcsn.util

expect object ImageCompressor {
    suspend fun compressImage(imageBytes: ByteArray, maxSizeKB: Int = 900): ByteArray
}