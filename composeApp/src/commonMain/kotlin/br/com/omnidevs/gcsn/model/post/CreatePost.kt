package br.com.omnidevs.gcsn.model.post

import EmbedRequest
import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.LabelObject
import br.com.omnidevs.gcsn.util.LabelsSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CreatePostRequest(
    val repo: String,
    val collection: String,
    val record: RecordRequest
)

@Serializable
data class RecordRequest(
    @SerialName("\$type") val type: String,
    val createdAt: String,
    val text: String,
    val facets: List<Facet>? = null,
    val reply: ReplyRef? = null,
    val embed: EmbedRequest? = null,
    val langs: List<String> = emptyList(),
    val tags: List<String>? = null,
    @Serializable(with = LabelsSerializer::class)
    val labels: RequestLabels = RequestLabels.empty()
)


@Serializable
data class RequestLabels(
    @Transient
    val type: String = "com.atproto.label.defs#selfLabels",
    private val values: List<Label> = emptyList()
) : List<Label> by values {

    companion object {
        fun create(vararg values: String): RequestLabels {
            return RequestLabels(values = values.map { Label(value = it) })
        }

        fun empty(): RequestLabels {
            return RequestLabels()
        }
    }
}