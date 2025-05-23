package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class RecordEmbed(
    val uri: String, // The URI of the record
    val cid: String // The CID of the record
)