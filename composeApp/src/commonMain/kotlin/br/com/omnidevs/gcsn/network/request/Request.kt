package br.com.omnidevs.gcsn.network.request

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val repo: String,
    val collection: String = "app.bsky.feed.post",
    val record: PostRecord
)

@Serializable
data class PostRecord(
    val text: String,
    val createdAt: String
)