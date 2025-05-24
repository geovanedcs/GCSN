package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
@SerialName("app.bsky.embed.video")
data class VideoEmbed(
    val aspectRatio: AspectRatio?,
    val video: VideoBlob?
)

@Serializable
data class VideoBlob(
    @SerialName("\$type") val type: String = "blob",
    val ref: BlobRef,
    val mimeType: String,
    val size: Long
)
