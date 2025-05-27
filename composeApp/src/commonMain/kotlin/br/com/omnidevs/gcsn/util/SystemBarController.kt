package br.com.omnidevs.gcsn.util

import androidx.compose.runtime.Composable

expect class SystemBarController() {
    @Composable
    fun setStatusBarColors(isDarkTheme: Boolean)
}