package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class ThreadGate(
    val uri: String,
    val cid: String,
    val record: ThreadGateRecord,
    val lists: List<ThreadgateList> = emptyList()
)

@Serializable
data class ThreadgateList(
    val uri: String,
    val cid: String? = null,
    val name: String? = null
)

@Serializable
data class ThreadGateRecord(
    @SerialName("\$type")
    val type: String,
    val createdAt: String,
    val post: String,
    val allow: List<ThreadGateRule> = emptyList(),
    val hiddenReplies: List<String> = emptyList()
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class ThreadGateRule

@Serializable
@SerialName("app.bsky.feed.threadgate#followingRule")
data class FollowingRule(val placeholder: String? = null) : ThreadGateRule()

@Serializable
@SerialName("app.bsky.feed.threadgate#followerRule")
data class FollowerRule(val placeholder: String? = null) : ThreadGateRule()

@Serializable
@SerialName("app.bsky.feed.threadgate#mentionRule")
data class MentionRule(val placeholder: String? = null) : ThreadGateRule()