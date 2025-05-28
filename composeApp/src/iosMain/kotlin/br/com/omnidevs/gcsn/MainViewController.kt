package br.com.omnidevs.gcsn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import br.com.omnidevs.gcsn.ui.screens.SplashScreen
import br.com.omnidevs.gcsn.ui.theme.AppTheme
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.SecureStorageProvider
import cafe.adriel.voyager.navigator.Navigator
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

// Add top-level function for Swift to call
@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "createMainViewController")
fun createMainViewController(): UIViewController {
    return ComposeUIViewController {
        val secureStorageProvider = remember { SecureStorageProvider() }

        LaunchedEffect(Unit) {
            AppDependencies.initialize(secureStorageProvider)
        }

        AppTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Navigator(screen = SplashScreen())
                }
            }
        }
    }
}

// Keep the class if needed elsewhere, but we'll primarily use the function above
class ComposeViewControllerFactory {
    fun createComposeViewController(): UIViewController = createMainViewController()
}