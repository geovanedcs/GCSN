package br.com.omnidevs.gcsn.network.api

import br.com.omnidevs.gcsn.network.request.CreatePostRequest
import br.com.omnidevs.gcsn.network.response.CreatePostResponse
import br.com.omnidevs.gcsn.network.response.FeedResponse
import br.com.omnidevs.gcsn.network.response.GetProfileResponse


class BlueskyApi(private val client: HttpClient) {

    suspend fun getProfile(actor: String): GetProfileResponse {
        return client.get("xrpc/app.bsky.actor.getProfile") {
            parameter("actor", actor)
        }.body()
    }

    suspend fun getAuthorFeed(actor: String, limit: Int = 20, cursor: String? = null): FeedResponse {
        return client.get("xrpc/app.bsky.feed.getAuthorFeed") {
            parameter("actor", actor)
            parameter("limit", limit)
            cursor?.let { parameter("cursor", it) }
        }.body()
    }

    suspend fun createPost(authorization: String, request: CreatePostRequest): CreatePostResponse {
        return client.post("xrpc/app.bsky.feed.post") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}