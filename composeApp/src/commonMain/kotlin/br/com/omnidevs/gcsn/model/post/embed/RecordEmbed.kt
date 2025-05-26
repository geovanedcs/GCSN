package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class RecordEmbed(
    val cid: String,
    val uri: String
)