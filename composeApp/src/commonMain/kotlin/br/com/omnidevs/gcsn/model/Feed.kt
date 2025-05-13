package br.com.omnidevs.gcsn.model

import br.com.omnidevs.gcsn.model.post.Post

data class Feed(
    val posts: List<Post>,
    val cursor: String? = null
)