package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.post.Record
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
    data class External(val external: ExternalEmbed?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.external#view")
    data class ExternalView(val external: ExternalEmbedView?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.recordWithMedia")
    data class RecordWithMedia(val record: RecordWithRecord?, val media: Embed?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.record")
    data class RecordWithRecord(val record: RecordValue?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.record#view")
    data class RecordView(val record: RecordViewRecord?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.recordWithMedia#view")
    data class RecordWithMediaView(
        val record: RecordViewWrapper?,
        val media: Embed?
    ) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.video")
    data class Video(val aspectRatio: AspectRatio?, val video: VideoBlob?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.video#view")
    data class VideoView(
        val cid: String?,
        val playlist: String?,
        val thumbnail: String?,
        val aspectRatio: AspectRatio?
    ) : Embed()

    @Serializable
    data class RecordViewWrapper(
        val record: RecordViewRecord?  // This now directly points to the viewRecord
    )
}
