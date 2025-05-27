package br.com.omnidevs.gcsn.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

actual class SystemBarController actual constructor() {
    @Composable
    actual fun setStatusBarColors(isDarkTheme: Boolean) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isDarkTheme

        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )

            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )

            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons,
                navigationBarContrastEnforced = false
            )
        }
    }
}