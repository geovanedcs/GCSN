package br.com.omnidevs.gcsn.network.response

import br.com.omnidevs.gcsn.model.post.Post
import kotlinx.serialization.Serializable
import br.com.omnidevs.gcsn.model.post.RecordEmbed

@Serializable
data class GetProfileResponse(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val description: String? = null,
    val avatar: String? = null,
    val indexedAt: String? = null,
    val followsCount: Int? = null,
    val followersCount: Int? = null,
    val postsCount: Int? = null
)

@Serializable
data class FeedResponse(
    val feed: List<FeedPostResponse>,
    val cursor: String? = null
)

@Serializable
data class FeedPostResponse(
    val post: Post,
    val reply: Post? = null
)
@Serializable
data class CreatePostResponse(
    val uri: String,
    val cid: String
)