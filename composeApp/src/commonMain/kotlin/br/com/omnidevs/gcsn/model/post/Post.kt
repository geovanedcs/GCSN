package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.post.embed.Embed
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Post(
    val uri: String,
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
    @SerialName("\$type") val viewType: String = "app.bsky.feed.defs#postView",
    val threadgate: Threadgate? = null
)