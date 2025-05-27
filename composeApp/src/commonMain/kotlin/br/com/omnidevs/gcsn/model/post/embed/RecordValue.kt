package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.post.ReplyRef
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
data class RecordValue(
    @SerialName("\$type")
    val type: String? = null,
    val text: String? = null,
    val createdAt: String? = null,
    val embed: Embed? = null,
    val langs: List<String>? = null,
    // Optional fields that might appear in some records
    val reply: ReplyRef? = null,
    val labels: Label? = null
)