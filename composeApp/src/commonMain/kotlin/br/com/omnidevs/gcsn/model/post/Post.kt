package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.actor.Author
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val uri: String, // Unique identifier for the post
    val cid: String, // Content identifier
    val author: Author, // The user who created the post
    val record: Record, // The post's content
    val embed: Embed? = null, // Embedded content (e.g., images, links)
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val likeCount: Int = 0,
    val quoteCount: Int = 0,
    val indexedAt: String,
    val viewer: Viewer? = null,
    val labels: List<Label>? = null,
)