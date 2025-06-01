package br.com.omnidevs.gcsn.model.actor

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.post.Viewer
import kotlinx.serialization.Serializable

@Serializable
data class Actor(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val associated: Associated,
    val viewer: Viewer,
    val labels: List<Label> = emptyList(),
    val createdAt: String,
    val description: String? = null,
    val indexedAt: String,
    val banner: String? = null,
    val followersCount: Int = 0,
    val followsCount: Int = 0,
    val postsCount: Int = 0
)

@Serializable
data class SearchActorsResponse(
    val actors: List<Actor>,
    val cursor: String? = null
)