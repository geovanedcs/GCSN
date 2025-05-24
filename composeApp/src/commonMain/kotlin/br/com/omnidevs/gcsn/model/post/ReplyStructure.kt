package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.actor.Author
import kotlinx.serialization.Serializable

@Serializable
data class ReplyStructure(
    val root: Post,
    val parent: Post,
    val grandparentAuthor: Author? = null
)
