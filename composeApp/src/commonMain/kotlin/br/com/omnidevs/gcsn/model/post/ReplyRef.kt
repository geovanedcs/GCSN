package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class ReplyRef(
    val cid: String,
    val uri: String
)
