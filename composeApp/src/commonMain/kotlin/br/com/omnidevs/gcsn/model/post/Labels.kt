package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
data class Labels(
    val selfLabels: List<String>? = null
)
