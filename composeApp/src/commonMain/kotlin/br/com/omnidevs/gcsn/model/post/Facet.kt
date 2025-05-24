package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Facet(
    val features: List<Feature>,
    val index: Index
)

@Serializable
data class Feature(
    @SerialName("\$type") val type: String,
    val tag: String? = null
)

@Serializable
data class Index(
    val byteEnd: Int,
    val byteStart: Int
)