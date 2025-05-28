package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(
    val repo: String,
    val collection: String,
    val record: Record
)