package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.post.ExternalEmbed
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class Embed {

    @Serializable
    @SerialName("app.bsky.embed.images")
    data class Image(val images: List<ImageDetail>?) : Embed()
    @Serializable
    @SerialName("app.bsky.embed.images#view")
    data class ImageView(val images: List<ImageViewDetail>?) : Embed()
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
