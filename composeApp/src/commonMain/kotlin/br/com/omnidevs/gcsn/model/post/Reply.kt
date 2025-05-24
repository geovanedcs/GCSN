package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Reply(
    val parent: ReplyRef,
    val root: ReplyRef
)