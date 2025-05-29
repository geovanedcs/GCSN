package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.network.api.BlueskyAuthApi
import br.com.omnidevs.gcsn.util.AppDependencies
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
                val api = BlueskyApi()
                val authApi = BlueskyAuthApi()
                val did = userData?.did
                AuthManager.accessToken = userData?.authToken
                AuthManager.refreshToken = userData?.refreshToken

                if (authService.validateToken()) {
                    navigator.replace(HomeScreen())
                }else{
                    try{
                       val refresh = authApi.refreshToken(userData?.refreshToken.toString())
                        AuthManager.accessToken = refresh?.accessJwt
                        AuthManager.refreshToken = refresh?.refreshJwt
                        authService.saveUserData(
                            refresh!!.accessJwt,
                            refresh.refreshJwt,
                            did.toString(),
                            userData?.handle.toString()
                        )
                        navigator.replace(HomeScreen())
                    }catch (e: Exception) {
                        e.printStackTrace()
                        navigator.replace(FirstStartScreen())
                    }
                }
            } else {
                navigator.replace(FirstStartScreen())
            }
        }
    }
}