package br.com.omnidevs.gcsn.model.post

import kotlinx.serialization.Serializable

@Serializable
sealed class Embed {
    data class Images(val images: List<Image>) : Embed()
    data class External(val external: ExternalEmbed) : Embed()
    data class Record(val record: RecordEmbed) : Embed()
    data class RecordWithMedia(val record: RecordEmbed, val media: Embed): Embed()
}
