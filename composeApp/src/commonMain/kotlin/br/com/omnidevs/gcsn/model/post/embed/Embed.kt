package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.post.Facet
import br.com.omnidevs.gcsn.model.post.ReplyRef
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class Embed {
    // Image embeds
    @Serializable
    @SerialName("app.bsky.embed.images")
    data class Images(val images: List<ImageDetail>) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.images#view")
    data class ImagesView(val images: List<ImageView>) : Embed()

    // External embeds
    @Serializable
    @SerialName("app.bsky.embed.external")
    data class External(val external: ExternalEmbed) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.external#view")
    data class ExternalView(val external: ExternalViewEmbed) : Embed()

    // Video embeds
    @Serializable
    @SerialName("app.bsky.embed.video")
    data class Video(val aspectRatio: AspectRatio, val video: Blob) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.video#view")
    data class VideoView(
        val cid: String, val playlist: String, val thumbnail: String, val aspectRatio: AspectRatio
    ) : Embed()

    // Record embeds
    @Serializable
    @SerialName("app.bsky.embed.record")
    data class Record(val record: EmbedRecord?) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.record#view")
    data class RecordView(val record: ViewRecord?) : Embed()

    // Record with Media embeds
    @Serializable
    @SerialName("app.bsky.embed.recordWithMedia")
    data class RecordWithMedia(val record: Record, val media: Embed) : Embed()

    @Serializable
    @SerialName("app.bsky.embed.recordWithMedia#view")
    data class RecordWithMediaView(val record: RecordView, val media: Embed) : Embed()
}

@Serializable
data class AspectRatio(
    val width: Int, val height: Int
)

@Serializable
data class ImageView(
    val thumb: String,
    val fullsize: String,
    val alt: String? = null,
    val aspectRatio: AspectRatio? = null
)

@Serializable
data class ExternalViewEmbed(
    val uri: String, val title: String, val description: String, val thumb: String? = null
)

@Serializable
data class EmbedRecord(
    val cid: String, val uri: String
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class ViewRecord {
    @Serializable
    @SerialName("app.bsky.embed.record#viewRecord")
    data class ViewNormal(
        val uri: String,
        val cid: String,
        val author: Author,
        val value: RecordValue? = null,
        val labels: List<Label>? = null,
        val embeds: List<Embed>? = null,
        val indexedAt: String,
        val likeCount: Int = 0,
        val replyCount: Int = 0,
        val repostCount: Int = 0,
        val quoteCount: Int = 0
    ) : ViewRecord()

    @Serializable
    @SerialName("app.bsky.embed.record#viewBlocked")
    data class ViewBlocked(
        val uri: String, val blocked: Boolean = true, val author: BlockedAuthor
    ) : ViewRecord()

    @Serializable
    @SerialName("app.bsky.embed.record#viewNotFound")
    data class ViewNotFound(
        val uri: String, val notFound: Boolean = true
    ) : ViewRecord()
}

@Serializable
data class BlockedAuthor(
    val did: String, val viewer: AuthorBlockedViewer? = null
)

@Serializable
data class AuthorBlockedViewer(
    val blockedBy: Boolean = false
)

@Serializable
data class RecordValue(
    @SerialName("\$type") val type: String,
    val text: String? = null,
    val createdAt: String,
    val embed: Embed? = null,
    val langs: List<String>? = null,
    val facets: List<Facet>? = null,
    val reply: ReplyRef? = null
)

@Serializable
data class ExternalEmbed(
    val uri: String, val title: String, val description: String, val thumb: Blob? = null
)