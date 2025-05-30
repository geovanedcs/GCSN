package br.com.omnidevs.gcsn.model

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val feed: List<FeedItem>,
    val cursor: String? = null
)