package br.com.omnidevs.gcsn.model.actor

import kotlinx.serialization.Serializable

@Serializable
data class Associated(
    val lists: Int = 0,
    val feedgens: Int = 0,
    val starterPacks: Int = 0,
    val labeler: Boolean = false,
    val chat: Chat? = null,
)
