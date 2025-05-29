package br.com.omnidevs.gcsn.model

data class ImageFile(
    val uri: String,
    val name: String,
    val mimeType: String,
    val size: Long
)