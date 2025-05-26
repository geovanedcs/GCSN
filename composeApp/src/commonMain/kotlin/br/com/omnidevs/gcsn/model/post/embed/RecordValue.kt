package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.post.Facet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordValue(
    @SerialName("\$type") val type: String,
    val createdAt: String,
    val embed: Embed? = null,
    val facets: List<Facet> = emptyList(),
    val text: String
)