package br.com.omnidevs.gcsn.util

expect class ImagePicker() {
    suspend fun pickImages(): List<ImageFile>
    suspend fun pickSingleImage(): ImageFile?
}

data class ImageFile(
    val uri: String,
    val name: String,
    val mimeType: String,
    val size: Long
)