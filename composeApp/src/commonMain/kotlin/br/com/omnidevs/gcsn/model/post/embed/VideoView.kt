package br.com.omnidevs.gcsn.model.post.embed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("app.bsky.embed.video#view")
data class VideoView(
    val cid: String,
    val playlist: String,
    val thumbnail: String,
    val aspectRatio: AspectRatio
)