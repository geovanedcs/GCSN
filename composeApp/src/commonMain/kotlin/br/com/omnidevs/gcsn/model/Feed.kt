package br.com.omnidevs.gcsn.model

import br.com.omnidevs.gcsn.model.post.Post
import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val posts: List<Post>,
    val cursor: String? = null
)