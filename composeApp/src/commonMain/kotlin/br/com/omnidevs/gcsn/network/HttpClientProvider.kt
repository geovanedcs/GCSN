package br.com.omnidevs.gcsn.network

import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.network.interceptor.AuthInterceptor
import br.com.omnidevs.gcsn.util.AuthService
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.observer.ResponseObserver
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object HttpClientProvider {
    private var client: HttpClient? = null
    private lateinit var authService: AuthService
    private val mutex = Mutex()

    val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = false
        explicitNulls = false
    }

    fun getClient(service: AuthService? = null): HttpClient {
        if (service != null) {
            authService = service
        } else if (!::authService.isInitialized) {
            throw IllegalStateException("É necessário inicializar o AuthService antes de usar o HttpClient")
        }

        if (client == null) {
            client = HttpClient {
                install(ContentNegotiation) {
                    json(json)
                }

                // Adicionar timeout para evitar esperas infinitas
                install(HttpTimeout) {
                    requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                    connectTimeoutMillis = 15.seconds.inWholeMilliseconds
                }

                // Instalar o interceptor de autenticação
                install(AuthInterceptor) {
                    authService = this@HttpClientProvider.authService
                }

                // Tratamento de respostas não autorizadas
                install(ResponseObserver) {
                    onResponse { response ->
                        if (response.status == HttpStatusCode.Unauthorized) {
                            // Usando mutex para evitar múltiplas verificações simultâneas
                            CoroutineScope(Dispatchers.IO).launch {
                                val currentState = AuthStateManager.authState.value
                                if (currentState != AuthState.TOKEN_REFRESHING) {
                                    mutex.withLock {
                                        if (AuthStateManager.authState.value != AuthState.TOKEN_REFRESHING) {
                                            AuthStateManager.setAuthState(AuthState.TOKEN_REFRESHING)
                                            try {
                                                val isValid = authService.validateToken()
                                                AuthStateManager.setAuthState(
                                                    if (isValid) AuthState.LOGGED_IN else AuthState.LOGGED_OUT
                                                )
                                            } catch (e: Exception) {
                                                AuthStateManager.setAuthState(AuthState.LOGGED_OUT)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                defaultRequest {
                    // Verificação mais segura para o token
                    val token = AuthManager.accessToken
                    if (!token.isNullOrEmpty()) {
                        headers.append(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }
        }
        return client!!
    }

    fun resetClient() {
        client?.close()
        client = null
    }
}