package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class ImageDetail(
    val alt: String? = null,
    val aspectRatio: AspectRatio,
    val image: Blob
)
