package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.actor.Author
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import kotlinx.serialization.Serializable

@Serializable
data class ReplyStructure(
    val root: Post,
    val parent: PostOrBlockedPost,
    val grandparentAuthor: Author? = null
)
