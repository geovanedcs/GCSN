package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("app.bsky.embed.recordWithMedia")
data class RecordWithMediaEmbed(
    @SerialName("\$type") val type: String = "app.bsky.embed.recordWithMedia",
    val media: RecordEmbed, // e.g., ImagesEmbed
    val record: RecordEmbedRecord
)

@Serializable
data class RecordEmbedRecord(
    @SerialName("\$type") val type: String = "app.bsky.embed.record",
    val record: RecordEmbed
)