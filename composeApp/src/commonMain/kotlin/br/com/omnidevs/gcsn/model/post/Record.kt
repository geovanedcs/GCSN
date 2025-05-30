package br.com.omnidevs.gcsn.model.post

import br.com.omnidevs.gcsn.model.post.embed.Embed
import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.LabelObject
import br.com.omnidevs.gcsn.util.LabelsSerializer
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
    @Serializable(with = LabelsSerializer::class)
    val labels: List<LabelObject> = emptyList()
)