import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class EmbedRequest{
    @Serializable
    @SerialName("app.bsky.embed.images")
    data class Images(val images: List<ImageDetailRequest>? = null) : EmbedRequest()

}

@Serializable
data class ImageDetailRequest(
    val alt: String = "",
    val image: BlobRefRequest
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("\$type")
sealed class BlobRefRequest {
    @Serializable
    @SerialName("blob")
    data class Blob(
        val ref: LinkRef,
        val mimeType: String? = null,
        val size: Long = 0L
    ) : BlobRefRequest()
}

@Serializable
data class LinkRef(
    @SerialName("\$link")
    val link: String
)