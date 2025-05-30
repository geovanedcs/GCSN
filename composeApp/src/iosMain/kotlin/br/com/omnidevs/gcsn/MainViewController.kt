package br.com.omnidevs.gcsn

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
import br.com.omnidevs.gcsn.util.IosMediaContentReader
import br.com.omnidevs.gcsn.util.SecureStorageProvider
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "createMainViewController")
fun createMainViewController(): UIViewController {

    val viewController = ComposeUIViewController {
        val secureStorageProvider = remember { SecureStorageProvider() }
        val mediaContentReader = remember { IosMediaContentReader() }

        LaunchedEffect(Unit) {
            AppDependencies.initialize(
                secureStorageProvider = secureStorageProvider,
                mediaContentReaderProvider = mediaContentReader
            )
        }

        AppTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Navigator(screen = SplashScreen()) { navigator ->
                        SlideTransition(navigator = navigator)
                    }
                }
            }
        }
    }

    return viewController
}

class ComposeViewControllerFactory {
    fun createComposeViewController(): UIViewController = createMainViewController()
}