package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Threadgate(
    val uri: String,
    val cid: String,
    val record: ThreadgateRecord,
    val lists: List<ThreadgateList> = emptyList()
)

@Serializable
data class ThreadgateList(
    val uri: String,
    val cid: String? = null,
    val name: String? = null
)

@Serializable
data class ThreadgateRecord(
    @SerialName("\$type") val type: String,
    val allow: List<ThreadgateRule>? = null,
    val createdAt: String,
    val hiddenReplies: List<String> = emptyList(),
    val post: String
)

@Serializable
data class ThreadgateRule(
    @SerialName("\$type") val type: String,
    val list: String? = null
)