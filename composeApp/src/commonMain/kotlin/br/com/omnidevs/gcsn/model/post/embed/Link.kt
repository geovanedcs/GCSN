package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Link(
    @SerialName("\$link") val link: String
)
