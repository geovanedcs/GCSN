package br.com.omnidevs.gcsn.model

import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import br.com.omnidevs.gcsn.model.post.ReplyStructure
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class FeedItem(
    val post: Post,
    val reply: ReplyStructure? = null,
    val reason: Reason? = null
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed interface Reason

@Serializable
@SerialName("app.bsky.feed.defs#reasonRepost")
data class RepostReason(
    val by: Author,
    val uri: String,
    val cid: String,
    val indexedAt: String
) : Reason