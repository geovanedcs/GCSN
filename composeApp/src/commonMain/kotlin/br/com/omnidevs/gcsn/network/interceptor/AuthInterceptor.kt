package br.com.omnidevs.gcsn.network.interceptor

import br.com.omnidevs.gcsn.util.AuthService
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.statement.HttpResponseContainer
import io.ktor.client.statement.HttpResponsePipeline
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthInterceptor(private val authService: AuthService) {
    class Config {
        lateinit var authService: AuthService
    }

    companion object Plugin : HttpClientPlugin<Config, AuthInterceptor> {
        override val key = AttributeKey<AuthInterceptor>("AuthInterceptor")

        override fun prepare(block: Config.() -> Unit): AuthInterceptor {
            val config = Config().apply(block)
            return AuthInterceptor(config.authService)
        }

        override fun install(plugin: AuthInterceptor, scope: HttpClient) {
            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
                val response = context.response
                if (response.status == HttpStatusCode.Unauthorized) {
                    // Evitamos runBlocking aqui
                    AuthStateManager.setAuthState(AuthState.TOKEN_REFRESHING)

                    // Lançamos uma coroutine para lidar com o refresh
                    CoroutineScope(Dispatchers.Default).launch {
                        val isValid = plugin.authService.validateToken()
                        if (!isValid) {
                            AuthStateManager.setAuthState(AuthState.LOGGED_OUT)
                        } else {
                            AuthStateManager.setAuthState(AuthState.LOGGED_IN)
                        }
                    }

                    // Continuamos com a resposta original
                    proceedWith(HttpResponseContainer(info, body))
                } else {
                    proceedWith(HttpResponseContainer(info, body))
                }
            }
        }
    }
}