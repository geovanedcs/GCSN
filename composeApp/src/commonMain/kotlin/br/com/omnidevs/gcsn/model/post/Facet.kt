package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Facet(
    val index: TextSlice,
    val type: String,
    val value: String
)
