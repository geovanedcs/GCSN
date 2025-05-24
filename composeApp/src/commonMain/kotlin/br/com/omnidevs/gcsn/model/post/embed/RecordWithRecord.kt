package br.com.omnidevs.gcsn.model.post.embed

import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.actor.Author
import kotlinx.serialization.Serializable

@Serializable
data class RecordViewRecord(
    val uri: String,
    val cid: String,
    val author: Author,
    val value: RecordValue?,
    val labels: List<Label> = emptyList(),
    val likeCount: Int = 0,
    val replyCount: Int = 0,
    val repostCount: Int = 0,
    val quoteCount: Int = 0,
    val indexedAt: String,
    val embeds: List<Embed>? = emptyList()
)