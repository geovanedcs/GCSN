package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Actor
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val uri: String, // Unique identifier for the post
    val cid: String, // Content identifier
    val author: Actor, // The user who created the post
    val text: String, // The post's content
    val createdAt: String, // Timestamp when the post was created
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val likeCount: Int = 0,
    val reply: ReplyRef? = null, // Reference to the post being replied to
    val embed: Embed? = null, // Embedded content (e.g., images, links)
    val labels: List<Label>? = null,
    val langs: List<String>? = null
)