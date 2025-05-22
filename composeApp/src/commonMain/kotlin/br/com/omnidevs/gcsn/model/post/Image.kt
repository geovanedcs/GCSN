package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val thumb: String,
    val fullsize: String,
    val alt: String,
    val aspectRatio: AspectRatio
)