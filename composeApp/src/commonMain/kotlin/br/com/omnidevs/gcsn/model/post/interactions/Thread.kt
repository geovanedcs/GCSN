package br.com.omnidevs.gcsn.model.post.interactions

import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import kotlinx.serialization.Serializable

@Serializable
data class ThreadViewPost(
    val thread: ThreadView
)

@Serializable
data class ThreadView(
    val post: Post,
    val parent: ThreadParent? = null,
    val replies: List<ThreadReply> = emptyList()
)

@Serializable
data class ThreadParent(
    val post: Post
)

@Serializable
data class ThreadReply(
    val post: Post
)