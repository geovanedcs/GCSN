package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class StrongRef(
    val uri: String,
    val cid: String
)
