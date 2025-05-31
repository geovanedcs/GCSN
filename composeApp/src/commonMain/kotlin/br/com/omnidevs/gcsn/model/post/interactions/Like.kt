package br.com.omnidevs.gcsn.model.post.interactions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LikeRequest(
    val repo: String,
    val collection: String = "app.bsky.feed.like",
    val record: LikeRecord
)

@Serializable
data class LikeRecord(
    @SerialName("\$type")
    val type: String = "app.bsky.feed.like",
    val subject: SubjectRef,
    val createdAt: String
)

@Serializable
data class SubjectRef(
    val uri: String,
    val cid: String
)

@Serializable
data class UnlikeRequest(
    val repo: String,
    val collection: String,
    val rkey: String
)