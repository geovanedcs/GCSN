package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class SplashScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authService = AppDependencies.authService

        LaunchedEffect(Unit) {
            val logged = authService.isUserAuthenticated()
            if (logged) {
                val userData = authService.getUserData()

                if (userData != null) {
                    // Carrega os tokens no AuthManager
                    AuthManager.accessToken = userData.authToken
                    AuthManager.refreshToken = userData.refreshToken

                    // Atualiza o estado global
                    AuthStateManager.setAuthState(AuthState.LOGGED_IN)

                    navigator.replace(HomeScreen())
                } else {
                    // Se userData for nulo, mesmo com logged=true, considerar como não autenticado
                    AuthStateManager.setAuthState(AuthState.LOGGED_OUT)
                    navigator.replace(FirstStartScreen())
                }
            } else {
                navigator.replace(FirstStartScreen())
            }
        }
    }
}