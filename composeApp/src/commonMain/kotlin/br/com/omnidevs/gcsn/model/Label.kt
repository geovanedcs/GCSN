package br.com.omnidevs.gcsn.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Label(
    val uri: String? = null,
    val cid: String? = null,
    @SerialName("val")
    val value: String,
    val neg: Boolean? = null,
    val src: String? = null,
    val cts: String? = null
)