package br.com.omnidevs.gcsn.model

data class Label(
    val uri: String? = null,
    val cid: String? = null,
    val val_field: String,
    val neg: Boolean? = null,
    val src: String? = null,
    val cts: String? = null
)