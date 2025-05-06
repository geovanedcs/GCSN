package br.com.omnidevs.gcsn.model.post

data class ExternalEmbed(
    val uri: String, // The URL
    val title: String, // Title of the linked content
    val description: String, // Description of the linked content
    val thumb: String // Thumbnail for the linked content
)