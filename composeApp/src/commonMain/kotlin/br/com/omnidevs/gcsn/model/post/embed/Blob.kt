package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlobResponse(val blob: Blob)

@Serializable
data class Blob(
    @SerialName("\$type") val type: String,
    val ref: Link,
    val mimeType: String,
    val size: Long
)

@Serializable
data class Link(
    @SerialName("\$link") val link: String
)
