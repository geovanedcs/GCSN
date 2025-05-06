package br.com.omnidevs.gcsn.model

data class Feed(
    val posts: List<Post>,
    val cursor: String? = null
)