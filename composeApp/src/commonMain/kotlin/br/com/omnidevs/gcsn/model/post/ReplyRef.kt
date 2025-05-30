package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class ReplyRef(
    val root: PostRef,
    val parent: PostRef
)

@Serializable
data class PostRef(
    val uri: String?,
    val cid: String?,
    val commit: Commit? = null,
    val validationStatus: String? = null
)

@Serializable
data class Commit(
    val cid: String,
    val rev: String
)