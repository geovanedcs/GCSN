package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class Facet(
    val features: List<FacetFeature>,
    val index: Index,
    @SerialName("\$type") val type: String? = "app.bsky.richtext.facet"
)

@Serializable
data class Index(
    val byteStart: Int,
    val byteEnd: Int,
    @SerialName("\$type") val type: String? = "app.bsky.richtext.facet#byteSlice"
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class FacetFeature

@Serializable
@SerialName("app.bsky.richtext.facet#tag")
data class TagFeature(val tag: String) : FacetFeature()

@Serializable
@SerialName("app.bsky.richtext.facet#link")
data class LinkFeature(val uri: String) : FacetFeature()

@Serializable
@SerialName("app.bsky.richtext.facet#mention")
data class MentionFeature(val did: String) : FacetFeature()