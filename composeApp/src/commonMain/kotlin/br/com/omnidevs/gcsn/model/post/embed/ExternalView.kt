package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class ExternalView(
    val uri: String,
    val title: String,
    val description: String,
    val thumb: String? = null
)