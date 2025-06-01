package br.com.omnidevs.gcsn.model

import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost
import kotlinx.serialization.Serializable

@Serializable
data class SearchPosts(
    val cursor: String? = null,
    val hitsTotal: Int? = null,
    val posts: List<PostOrBlockedPost.Post>? = emptyList()
)