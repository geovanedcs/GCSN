package br.com.omnidevs.gcsn.network.api

import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.HttpClientProvider
import br.com.omnidevs.gcsn.network.request.CreatePostRequest
import br.com.omnidevs.gcsn.network.response.CreatePostResponse
import br.com.omnidevs.gcsn.network.response.FollowResponse
import br.com.omnidevs.gcsn.network.response.HandleAvailabilityResponse
import br.com.omnidevs.gcsn.network.response.LikeResponse
import br.com.omnidevs.gcsn.network.response.NotificationResponse
import br.com.omnidevs.gcsn.network.response.RegisterAccountResponse
import br.com.omnidevs.gcsn.network.response.SearchActorsResponse
import br.com.omnidevs.gcsn.network.response.UnfollowResponse
import br.com.omnidevs.gcsn.network.response.UnlikeResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class BlueskyApi {

    private val client = HttpClientProvider.client

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

    suspend fun createPost(authorization: String, request: CreatePostRequest): CreatePostResponse {
        return client.post("xrpc/app.bsky.feed.post") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getTimeline(limit: Int = 20, cursor: String? = null): Feed {
        return client.get("xrpc/app.bsky.feed.getTimeline") {
            url.parameters.append("limit", limit.toString())
            cursor?.let { url.parameters.append("cursor", it) }
        }.body()
    }

    suspend fun getNotifications(limit: Int = 20, cursor: String? = null): NotificationResponse {
        return client.get("xrpc/app.bsky.notification.list") {
            url.parameters.append("limit", limit.toString())
            cursor?.let { url.parameters.append("cursor", it) }
        }.body()
    }

    suspend fun follow(authorization: String, actor: String): FollowResponse {
        return client.post("xrpc/app.bsky.graph.follow") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(mapOf("actor" to actor))
        }.body()
    }

    suspend fun unfollow(authorization: String, followUri: String): UnfollowResponse {
        return client.post("xrpc/app.bsky.graph.unfollow") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(mapOf("uri" to followUri))
        }.body()
    }

    suspend fun like(authorization: String, postUri: String, cid: String): LikeResponse {
        return client.post("xrpc/app.bsky.feed.like") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(mapOf("subject" to mapOf("uri" to postUri, "cid" to cid)))
        }.body()
    }

    suspend fun unlike(authorization: String, likeUri: String): UnlikeResponse {
        return client.post("xrpc/app.bsky.feed.unlike") {
            header(HttpHeaders.Authorization, authorization)
            contentType(ContentType.Application.Json)
            setBody(mapOf("uri" to likeUri))
        }.body()
    }

    suspend fun searchActors(query: String, limit: Int = 20): SearchActorsResponse {
        return client.get("xrpc/app.bsky.actor.searchActors") {
            url.parameters.append("query", query)
            url.parameters.append("limit", limit.toString())
        }.body()
    }

    suspend fun registerAccount(
        email: String,
        password: String,
        handle: String,
        inviteCode: String? = null
    ): RegisterAccountResponse {
        return client.post("xrpc/app.bsky.actor.createAccount") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password,
                    "handle" to handle,
                    "inviteCode" to inviteCode
                )
            )
        }.body()
    }

    suspend fun checkHandleAvailability(handle: String): HandleAvailabilityResponse {
        return client.get("xrpc/app.bsky.actor.checkHandle") {
            url.parameters.append("handle", handle)
        }.body()
    }
}