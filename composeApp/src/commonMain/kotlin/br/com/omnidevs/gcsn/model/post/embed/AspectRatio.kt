package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class AspectRatio(
    val width: Int,
    val height: Int
)
