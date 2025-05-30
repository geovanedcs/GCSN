package br.com.omnidevs.gcsn.network.api

import BlobRefRequest
import EmbedRequest
import ImageDetailRequest
import LinkRef
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.ImageFile
import br.com.omnidevs.gcsn.model.Label
import br.com.omnidevs.gcsn.model.LabelObject
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.post.CreatePostRequest
import br.com.omnidevs.gcsn.model.post.PostRef
import br.com.omnidevs.gcsn.model.post.RecordRequest
import br.com.omnidevs.gcsn.model.post.RequestLabels
import br.com.omnidevs.gcsn.model.post.embed.BlobResponse
import br.com.omnidevs.gcsn.network.HttpClientProvider
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.ImageCompressor
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class BlueskyApi {

    private val client = HttpClientProvider.getClient(AppDependencies.authService)

    suspend fun getProfile(actor: String): Actor {
        return client.get("https://bsky.social/xrpc/app.bsky.actor.getProfile") {
            url.parameters.append("actor", actor)
        }.body()
    }

    suspend fun getAuthorFeed(actor: String, limit: Int = 20, cursor: String? = null): Feed {
        val response = client.get("https://bsky.social/xrpc/app.bsky.feed.getAuthorFeed") {
            url.parameters.append("actor", actor)
            url.parameters.append("limit", limit.toString())
        }
        return response.body()
    }

    suspend fun getFeed(feed: String, limit: Int = 20, cursor: String? = null): Feed {
        val response = client.get("https://bsky.social/xrpc/app.bsky.feed.getFeed") {
            url.parameters.append("feed", feed)
            url.parameters.append("limit", limit.toString())
            cursor?.let { url.parameters.append("cursor", it) }
        }
        return response.body()
    }

    private suspend fun uploadBlob(imageUri: String, mimeType: String? = null): Pair<BlobResponse, String> {
        val authService = AppDependencies.authService
        val isValid = authService.validateToken()
        if (!isValid) {
            throw IllegalStateException("Sessão inválida ou expirada. Por favor, faça login novamente.")
        }

        // Get original image bytes
        val originalBytes = getImageBytesFromUri(imageUri)

        // Compress image before uploading - uses platform-specific implementation
        val compressedBytes = ImageCompressor.compressImage(originalBytes, 900)

        // Our compression always outputs JPEG format
        val determinedMimeType = "image/jpeg"

        println("Original size: ${originalBytes.size / 1024}KB, Compressed: ${compressedBytes.size / 1024}KB")

        // Send to API
        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.uploadBlob") {
            header(HttpHeaders.ContentType, determinedMimeType)
            setBody(compressedBytes)
        }

        return Pair(response.body<BlobResponse>(), determinedMimeType)
    }

    suspend fun createPost(text: String, images: List<ImageFile>): PostRef {
        // Processar as imagens, se houver
        val embed = if (images.isNotEmpty()) {
            val imageDetailRequests = images.mapNotNull { imageFile ->
                try {
                    val (blobResponse, actualMimeType) = uploadBlob(
                        imageUri = imageFile.uri,
                        mimeType = imageFile.mimeType
                    )

                    ImageDetailRequest(
                        alt = "",
                        image = BlobRefRequest.Blob(
                            ref = LinkRef(link = blobResponse.blob.ref.link),
                            mimeType = actualMimeType,  // Use the actual MIME type from upload
                            size = blobResponse.blob.size
                        )
                    )
                } catch (_: Exception) {
                    null
                }
            }

            if (imageDetailRequests.isEmpty()) null
            else EmbedRequest.Images(
                images = imageDetailRequests
            )
        } else null

        val timestamp = Clock.System.now().toString()

        val did = AppDependencies.authService.getUserData()?.did
            ?: throw IllegalStateException("Usuário não autenticado. Por favor, faça login.")

        val createPostRequest = CreatePostRequest(
            repo = did,
            collection = "app.bsky.feed.post",
            record = RecordRequest(
                type = "app.bsky.feed.post",
                text = text,
                createdAt = timestamp,
                embed = embed,
                facets = null,
                reply = null,
                langs = emptyList(),
                tags = null,
                labels = RequestLabels(
                    type = "com.atproto.label.defs#selfLabels",
                    values = listOf(
                        Label(value = "gcsn"),
                        Label(value = "cosplay")
                    )
                )
            )
        )
        val jsonString = Json.encodeToString(CreatePostRequest.serializer(), createPostRequest)
        println("Request payload: $jsonString")

        return client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(createPostRequest)
        }.body()
    }

    private suspend fun getImageBytesFromUri(imageUri: String): ByteArray {
        return getCachedBytes(imageUri) ?: AppDependencies.mediaContentReader.getMediaBytes(imageUri)
    }

    suspend fun getTimeline(limit: Int = 20, cursor: String? = null): Feed {
        return client.get("https://bsky.social/xrpc/app.bsky.feed.getTimeline") {
            url.parameters.append("limit", limit.toString())
            cursor?.let { url.parameters.append("cursor", it) }
        }.body()
    }

//    suspend fun getNotifications(limit: Int = 20, cursor: String? = null): NotificationResponse {
//        return client.get("xrpc/app.bsky.notification.list") {
//            url.parameters.append("limit", limit.toString())
//            cursor?.let { url.parameters.append("cursor", it) }
//        }.body()
//    }

    //    suspend fun follow(authorization: String, actor: String): FollowResponse {
//        return client.post("xrpc/app.bsky.graph.follow") {
//            header(HttpHeaders.Authorization, authorization)
//            contentType(ContentType.Application.Json)
//            setBody(mapOf("actor" to actor))
//        }.body()
//    }
//
//    suspend fun unfollow(authorization: String, followUri: String): UnfollowResponse {
//        return client.post("xrpc/app.bsky.graph.unfollow") {
//            header(HttpHeaders.Authorization, authorization)
//            contentType(ContentType.Application.Json)
//            setBody(mapOf("uri" to followUri))
//        }.body()
//    }
//
//    suspend fun like(authorization: String, postUri: String, cid: String): LikeResponse {
//        return client.post("xrpc/app.bsky.feed.like") {
//            header(HttpHeaders.Authorization, authorization)
//            contentType(ContentType.Application.Json)
//            setBody(mapOf("subject" to mapOf("uri" to postUri, "cid" to cid)))
//        }.body()
//    }
//
//    suspend fun unlike(authorization: String, likeUri: String): UnlikeResponse {
//        return client.post("xrpc/app.bsky.feed.unlike") {
//            header(HttpHeaders.Authorization, authorization)
//            contentType(ContentType.Application.Json)
//            setBody(mapOf("uri" to likeUri))
//        }.body()
//    }
//
//    suspend fun searchActors(query: String, limit: Int = 20): SearchActorsResponse {
//        return client.get("xrpc/app.bsky.actor.searchActors") {
//            url.parameters.append("query", query)
//            url.parameters.append("limit", limit.toString())
//        }.body()
//    }
//
//    suspend fun registerAccount(
//        email: String,
//        password: String,
//        handle: String,
//        inviteCode: String? = null
//    ): RegisterAccountResponse {
//        return client.post("xrpc/app.bsky.actor.createAccount") {
//            contentType(ContentType.Application.Json)
//            setBody(
//                mapOf(
//                    "email" to email,
//                    "password" to password,
//                    "handle" to handle,
//                    "inviteCode" to inviteCode
//                )
//            )
//        }.body()
//    }
//
//    suspend fun checkHandleAvailability(handle: String): HandleAvailabilityResponse {
//        return client.get("xrpc/app.bsky.actor.checkHandle") {
//            url.parameters.append("handle", handle)
//        }.body()
//    }
    companion object {
        private val imageByteCache = mutableMapOf<String, ByteArray>()

        fun cacheImageBytes(uri: String, bytes: ByteArray) {
            imageByteCache[uri] = bytes
        }

        fun getCachedBytes(uri: String): ByteArray? {
            return imageByteCache[uri]
        }

        fun clearCache() {
            imageByteCache.clear()
        }
    }
}