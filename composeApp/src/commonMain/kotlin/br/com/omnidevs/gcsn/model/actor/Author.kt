package br.com.omnidevs.gcsn.model.actor

import br.com.omnidevs.gcsn.model.Label
import kotlinx.serialization.Serializable

@Serializable
data class Author(
    val did: String,
    val handle: String,
    val displayName: String? = null,
    val avatar: String? = null,
    val viewer: AuthorViewer? = null,
    val labels: List<Label> = emptyList(),
    val createdAt: String
)