package br.com.omnidevs.gcsn.network.response

import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.post.Post
import kotlinx.serialization.Serializable

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