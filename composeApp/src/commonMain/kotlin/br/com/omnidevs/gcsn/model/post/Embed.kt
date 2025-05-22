package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("\$type")
sealed class Embed {
    @Serializable
    @SerialName("app.bsky.embed.images#view")
    data class ImagesView(val images: List<Image>) : Embed()
    @Serializable
    @SerialName("app.bsky.embed.external")
    data class External(val external: ExternalEmbed) : Embed()
    @Serializable
    @SerialName("app.bsky.embed.record")
    data class RecordRef(val record: RecordEmbed) : Embed()
    @Serializable
    @SerialName("app.bsky.embed.recordWithMedia")
    data class RecordWithMedia(val record: RecordEmbed, val media: Embed) : Embed()
}
