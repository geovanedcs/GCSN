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
import br.com.omnidevs.gcsn.util.IosMediaContentReader
import br.com.omnidevs.gcsn.util.SecureStorageProvider
import cafe.adriel.voyager.navigator.Navigator
import dev.icerock.moko.media.picker.ios.MediaPickerController
import dev.icerock.moko.permissions.ios.PermissionsController
import platform.UIKit.UIViewController
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "createMainViewController")
fun createMainViewController(): UIViewController {
    val permissionsController = PermissionsController()
    val mediaPickerController = MediaPickerController(permissionsController)

    val viewController = ComposeUIViewController {
        val secureStorageProvider = remember { SecureStorageProvider() }
        val mediaContentReader = remember { IosMediaContentReader() }
        remember { mediaPickerController }

        LaunchedEffect(Unit) {
            AppDependencies.initialize(
                secureStorageProvider = secureStorageProvider,
                mediaPickerControllerProvider = mediaPickerController,
                mediaContentReaderProvider = mediaContentReader
            )
        }

        AppTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    Navigator(screen = SplashScreen())
                }
            }
        }
    }

    mediaPickerController.bind(viewController)

    return viewController
}

class ComposeViewControllerFactory {
    fun createComposeViewController(): UIViewController = createMainViewController()
}