package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.post.embed.Embed
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed interface PostOrBlockedPost {
    val uri: String
}

@Serializable
@SerialName("app.bsky.feed.defs#postView")
data class Post(
    override val uri: String,
    val cid: String,
    val author: Author,
    val record: Record,
    val embed: Embed? = null,
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val likeCount: Int = 0,
    val quoteCount: Int = 0,
    val indexedAt: String,
    val viewer: Viewer,
    val labels: List<Label>? = emptyList(),
    val threadgate: ThreadGate? = null,
): PostOrBlockedPost

@Serializable
@SerialName("app.bsky.feed.defs#blockedPost")
data class BlockedPost(
    override val uri: String,
    val blocked: Boolean,
    val author: BlockedAuthor
) : PostOrBlockedPost

@Serializable
data class BlockedAuthor(
    val did: String,
    val viewer: BlockedViewer
)

@Serializable
data class BlockedViewer(
    val blockedBy: Boolean
)