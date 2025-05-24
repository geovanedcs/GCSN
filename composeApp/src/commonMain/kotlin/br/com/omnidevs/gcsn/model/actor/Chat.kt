package br.com.omnidevs.gcsn.model.actor

import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val allowIncoming: String? = null,
)
