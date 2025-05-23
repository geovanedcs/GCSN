package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class TextSlice(
    val start: Int,
    val end: Int
)