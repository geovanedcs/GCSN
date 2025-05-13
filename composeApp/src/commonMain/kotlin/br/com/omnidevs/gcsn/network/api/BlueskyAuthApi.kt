package br.com.omnidevs.gcsn.network.api

import br.com.omnidevs.gcsn.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class BlueskyAuthApi {

    private val client = HttpClientProvider.client

    suspend fun login(email: String, password: String): AuthResponse {
        return client.post("xrpc/com.atproto.server.createSession") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("identifier" to email, "password" to password))
        }.body()
    }
}

@Serializable
data class AuthResponse(
    val accessJwt: String,
    val refreshJwt: String,
    val handle: String,
    val did: String
)

object AuthManager {
    var accessToken: String? = null
}