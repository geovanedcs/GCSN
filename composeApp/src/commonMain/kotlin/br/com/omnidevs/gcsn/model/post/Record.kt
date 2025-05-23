package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.post.embed.Embed
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(
    @SerialName("\$type") val type: String,
    val createdAt: String,
    val text: String,
    val facets: List<Facet>? = null,
    val reply: ReplyRef? = null,
    val embed: Embed? = null,
    val langs: List<String> = emptyList(),
    val tags: List<String>? = null,
    val labels: Labels? = null
)