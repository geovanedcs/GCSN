package br.com.omnidevs.gcsn.network.request

import br.com.omnidevs.gcsn.model.post.Post
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val repo: String,
    val collection: String = "app.bsky.feed.post",
    val record: Post
)

