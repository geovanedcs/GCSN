package br.com.omnidevs.gcsn.model.actor

import kotlinx.serialization.Serializable

@Serializable
data class AuthorViewer(
    val muted: Boolean? = null,
    val blockedBy: Boolean? = null,
    val threadMuted: Boolean? = null,
)