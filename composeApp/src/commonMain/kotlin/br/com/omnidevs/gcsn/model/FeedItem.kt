package br.com.omnidevs.gcsn.model

import br.com.omnidevs.gcsn.model.post.Post
import br.com.omnidevs.gcsn.model.post.ReplyStructure
import kotlinx.serialization.Serializable

@Serializable
data class FeedItem(
    val post: Post,
    val reply: ReplyStructure? = null
)