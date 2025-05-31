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
import br.com.omnidevs.gcsn.model.post.interactions.CreateRecordResponse
import br.com.omnidevs.gcsn.model.post.interactions.FollowRecord
import br.com.omnidevs.gcsn.model.post.interactions.FollowRequest
import br.com.omnidevs.gcsn.model.post.interactions.LikeRecord
import br.com.omnidevs.gcsn.model.post.interactions.LikeRequest
import br.com.omnidevs.gcsn.model.post.interactions.SubjectRef
import br.com.omnidevs.gcsn.model.post.interactions.ThreadViewPost
import br.com.omnidevs.gcsn.model.post.interactions.UnfollowRequest
import br.com.omnidevs.gcsn.model.post.interactions.UnlikeRequest
import br.com.omnidevs.gcsn.network.HttpClientProvider
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.ImageCompressor
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.text.get

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

    suspend fun likePost(postUri: String, cid: String): PostRef {
        val did = AppDependencies.authService.getUserData()?.did
            ?: throw IllegalStateException("Usuário não autenticado. Por favor, faça login.")

        val timestamp = Clock.System.now().toString()

        val likeRequest = LikeRequest(
            repo = did,
            collection = "app.bsky.feed.like",
            record = LikeRecord(
                subject = SubjectRef(
                    uri = postUri,
                    cid = cid
                ),
                createdAt = timestamp
            )
        )

        return client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(likeRequest)
        }.body()
    }

    suspend fun unlikePost(likeUri: String): Boolean {
        val did = AppDependencies.authService.getUserData()?.did
            ?: throw IllegalStateException("Usuário não autenticado. Por favor, faça login.")

        val unlikeRequest = UnlikeRequest(
            repo = did,
            collection = "app.bsky.feed.like",
            rkey = extractRkeyFromUri(likeUri)
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.deleteRecord") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(unlikeRequest)
        }

        return response.status.value == 200
    }

    private fun extractRkeyFromUri(uri: String): String {
        // AtProto URIs follow the format: at://did:plc:something/collection/rkey
        return uri.substringAfterLast('/')
    }

    suspend fun followUser(actor: String): CreateRecordResponse {
        val did = AppDependencies.authService.getUserData()?.did
            ?: throw IllegalStateException("Usuário não autenticado. Por favor, faça login.")

        val timestamp = Clock.System.now().toString()

        val followRequest = FollowRequest(
            repo = did,
            collection = "app.bsky.graph.follow",
            record = FollowRecord(
                type = "app.bsky.graph.follow",
                subject = actor, // Actor is the DID of the user to follow
                createdAt = timestamp
            )
        )

        return client.post("https://bsky.social/xrpc/com.atproto.repo.createRecord") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(followRequest)
        }.body()
    }

    suspend fun unfollowUser(followUri: String): Boolean {
        val did = AppDependencies.authService.getUserData()?.did
            ?: throw IllegalStateException("Usuário não autenticado. Por favor, faça login.")

        val unfollowRequest = UnfollowRequest(
            repo = did,
            collection = "app.bsky.graph.follow",
            rkey = extractRkeyFromUri(followUri)
        )

        val response = client.post("https://bsky.social/xrpc/com.atproto.repo.deleteRecord") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(unfollowRequest)
        }

        return response.status.value == 200
    }

    suspend fun getThreadView(uri: String): ThreadViewPost {
        return client.get("https://bsky.social/xrpc/app.bsky.feed.getPostThread") {
            parameter("uri", uri)
        }.body()
    }

//    suspend fun getNotifications(limit: Int = 20, cursor: String? = null): NotificationResponse {
//        return client.get("xrpc/app.bsky.notification.list") {
//            url.parameters.append("limit", limit.toString())
//            cursor?.let { url.parameters.append("cursor", it) }
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