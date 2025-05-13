package br.com.omnidevs.gcsn.network.response

import br.com.omnidevs.gcsn.model.post.Post
import kotlinx.serialization.Serializable

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

@Serializable
data class NotificationResponse(
    val notifications: List<Notification>,
    val cursor: String? = null
)

@Serializable
data class Notification(
    val id: String,
    val type: String,
    val actor: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class FollowResponse(
    val uri: String,
    val cid: String
)

@Serializable
data class UnfollowResponse(
    val success: Boolean
)

@Serializable
data class LikeResponse(
    val uri: String,
    val cid: String
)

@Serializable
data class UnlikeResponse(
    val success: Boolean
)

@Serializable
data class SearchActorsResponse(
    val actors: List<Actor>
)

@Serializable
data class Actor(
    val id: String,
    val displayName: String,
    val handle: String,
    val avatar: String? = null
)

@Serializable
data class RegisterAccountResponse(
    val success: Boolean,
    val handle: String? = null,
    val error: String? = null
)

@Serializable
data class HandleAvailabilityResponse(
    val available: Boolean,
    val reason: String? = null
)