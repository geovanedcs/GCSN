package br.com.omnidevs.gcsn.model.actor

data class Actor(
    val did: String, // Decentralized Identifier (unique user ID)
    val handle: String, // User's handle (username)
    val displayName: String? = null, // User's display name
    val description: String? = null, // User's bio/description
    val avatar: String? = null, // URL to user's avatar image
    val banner: String? = null, // URL to user's banner image
    val followersCount: Int = 0,
    val followsCount: Int = 0,
    val postsCount: Int = 0,
    val indexedAt: String? = null
)
