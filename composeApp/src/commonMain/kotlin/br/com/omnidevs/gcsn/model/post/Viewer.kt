package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Viewer(
    // General viewer properties
    val muted: Boolean? = null,
    val blockedBy: Boolean? = null,
    val blocking: Boolean? = null,

    // Post-specific viewer properties
    val like: String? = null,  // URI of the like record if the viewer liked the post
    val repost: String? = null,  // URI of the repost record if the viewer reposted
    val threadMuted: Boolean? = null,
    val replyDisabled: Boolean? = null,
    val embeddingDisabled: Boolean? = null,

    // Profile-specific viewer properties
    val following: String? = null,  // URI of the follow record if the viewer follows the profile
    val followedBy: String? = null  // URI of the follow record if the profile follows the viewer
)