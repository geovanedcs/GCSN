package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class ImageViewDetail(
    val thumb: String? = null,
    val fullsize: String? = null,
    val alt: String? = null,
    val aspectRatio: AspectRatio
)
