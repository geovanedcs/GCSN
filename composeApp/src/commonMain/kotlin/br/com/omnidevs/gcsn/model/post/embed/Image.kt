package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val images: List<ImageDetail>
)
