package br.com.omnidevs.gcsn.network.api

import br.com.omnidevs.gcsn.network.HttpClientProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class BlueskyAuthApi {

    private val client = HttpClientProvider.client

    suspend fun login(email: String, password: String): AuthResponse? {
        return try {
            client.post("https://bsky.social/xrpc/com.atproto.server.createSession") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("identifier" to email, "password" to password))
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun validateToken(): Boolean {
        return try {
            val response = client.get("https://bsky.social/xrpc/com.atproto.server.getSession")

            when (response.status.value) {
                200 -> true
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun refreshToken(refreshToken: String): AuthResponse? {
        return try {
            HttpClient().post("https://bsky.social/xrpc/com.atproto.server.refreshSession") {
                headers.append(HttpHeaders.Authorization, "Bearer $refreshToken")
            }.body()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Serializable
data class AuthResponse(
    val did: String,
    val didDoc: DidDoc? = null,
    val handle: String,
    val email: String? = null,
    val emailConfirmed: Boolean? = null,
    val emailAuthFactor: Boolean? = null,
    val accessJwt: String,
    val refreshJwt: String,
    val active: Boolean? = null
)

@Serializable
data class DidDoc(
    @SerialName("@context")
    val context: List<String>,
    val id: String,
    val alsoKnownAs: List<String>? = null,
    val verificationMethod: List<VerificationMethod>? = null,
    val service: List<Service>? = null
)

@Serializable
data class VerificationMethod(
    val id: String,
    val type: String,
    val controller: String,
    val publicKeyMultibase: String
)

@Serializable
data class Service(
    val id: String,
    val type: String,
    val serviceEndpoint: String
)

object AuthManager {
    var accessToken: String? = null
    var refreshToken: String? = null
}