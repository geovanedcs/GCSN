package br.com.omnidevs.gcsn.util

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator

suspend fun Navigator.navigateAuthProtected(
    screen: Screen,
    fallbackScreen: Screen,
    validateToken: Boolean = false
) {
    val authService = AppDependencies.authService

    if (authService.isUserAuthenticated() && (!validateToken || authService.validateToken())) {
        this.push(screen)
    } else {
        this.push(fallbackScreen)
    }
}

suspend fun Navigator.navigateWithTokenValidation(
    screen: Screen,
    fallbackScreen: Screen
) {
    navigateAuthProtected(screen, fallbackScreen, validateToken = true)
}