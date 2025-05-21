package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Viewer(
    val threadMuted: Boolean? = null,
    val embeddingDisabled: Boolean? = null
)