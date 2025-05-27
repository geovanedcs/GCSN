package br.com.omnidevs.gcsn.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.setStatusBarStyle

actual class SystemBarController actual constructor() {
    @Composable
    actual fun setStatusBarColors(isDarkTheme: Boolean) {
        DisposableEffect(isDarkTheme) {
            val statusBarStyle = if (isDarkTheme) {
                UIStatusBarStyleLightContent
            } else {
                UIStatusBarStyleDarkContent
            }

            UIApplication.sharedApplication.setStatusBarStyle(statusBarStyle, animated = true)

            onDispose {}
        }
    }
}