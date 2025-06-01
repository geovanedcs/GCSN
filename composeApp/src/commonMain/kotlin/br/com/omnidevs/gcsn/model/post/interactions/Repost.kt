package br.com.omnidevs.gcsn.model.post.interactions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreateRepostRequest(
    val repo: String,
    val collection: String = "app.bsky.feed.repost",
    val record: RepostRecord
)


@Serializable
data class RepostRecord(
    @SerialName("\$type") val type: String = "app.bsky.feed.repost",
    val subject: RepostSubject,
    val createdAt: String
)

@Serializable
data class RepostSubject(
    val uri: String,
    val cid: String
)

@Serializable
data class DeleteRepostRequest(
    @SerialName("repo") val repo: String,
    @SerialName("collection") val collection: String = "app.bsky.feed.repost",
    @SerialName("rkey") val rkey: String
)