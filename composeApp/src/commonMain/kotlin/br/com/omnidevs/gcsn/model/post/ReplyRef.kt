package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class ReplyRef(
    val root: RecordEmbed,
    val parent: RecordEmbed
)