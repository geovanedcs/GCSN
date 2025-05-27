package br.com.omnidevs.gcsn.model.post.embed


import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Author
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class ViewRecord {}

@Serializable
@SerialName("app.bsky.embed.record#viewRecord")
data class RecordViewRecord(
    val uri: String,
    val cid: String,
    val author: Author,
    val value: RecordValue?,
    val labels: List<Label> = emptyList(),
    val embeds: List<Embed>? = emptyList(),
    val indexedAt: String,
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val quoteCount: Int = 0
)

@Serializable
@SerialName("app.bsky.embed.record#viewBlocked")
data class ViewBlocked(
    val uri: String,
    val blocked: Boolean = true,
    val author: BlockedAuthor
) : ViewRecord()

@Serializable
@SerialName("app.bsky.embed.record#viewNotFound")
data class ViewNotFound(
    val uri: String,
    val notFound: Boolean = true
) : ViewRecord()

@Serializable
data class BlockedAuthor(
    val did: String,
    val viewer: AuthorBlockedViewer? = null
)

@Serializable
data class AuthorBlockedViewer(
    val blockedBy: Boolean = false
)