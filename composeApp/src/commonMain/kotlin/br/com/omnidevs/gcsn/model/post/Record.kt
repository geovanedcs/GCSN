package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Record(
    @SerialName("\$type") val type: String,
    val createdAt: String,
    val embed: RecordEmbed? = null,
    val langs: List<String> = emptyList(),
    val text: String
)