package br.com.omnidevs.gcsn.model.post.interactions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowRequest(
    val repo: String,
    val collection: String = "app.bsky.graph.follow",
    val record: FollowRecord
)

@Serializable
data class FollowRecord(
    @SerialName("\$type") val type: String = "app.bsky.graph.follow",
    val subject: String, // DID of user to follow
    val createdAt: String // ISO timestamp
)

@Serializable
data class CreateRecordResponse(
    val uri: String,
    val cid: String
)

@Serializable
data class UnfollowRequest(
    val repo: String,
    val collection: String,
    val rkey: String // URI of the follow record to unfollow
)