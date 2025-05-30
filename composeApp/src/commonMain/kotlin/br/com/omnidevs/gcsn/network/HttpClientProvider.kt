package br.com.omnidevs.gcsn.network

import br.com.omnidevs.gcsn.network.api.AuthManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientProvider {
    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = false
        explicitNulls = false
    }
    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            AuthManager.accessToken?.let { token ->
                headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}