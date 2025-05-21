package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val thumb: String, // URL to a thumbnail version
    val fullsize: String, // URL to the full-size image
    val alt: String // Alternative text for accessibility
)