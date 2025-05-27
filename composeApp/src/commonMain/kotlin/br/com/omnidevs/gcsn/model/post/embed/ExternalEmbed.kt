package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ExternalEmbed(
    val uri: String, // The URL
    val title: String, // Title of the linked content
    val description: String, // Description of the linked content
    val thumb: BlobImage? // Thumbnail for the linked content
)

@Serializable
data class BlobImage(
    @SerialName("\$type") val type: String = "blob",
    val ref: BlobRef,
    val mimeType: String,
    val size: Long
)

@Serializable
data class BlobRef(
    @SerialName("\$link") val link: String
)

@Serializable
data class ExternalEmbedView(
    val uri: String, // The URL
    val title: String, // Title of the linked content
    val description: String, // Description of the linked content
    val thumb: String? // Thumbnail for the linked content
)