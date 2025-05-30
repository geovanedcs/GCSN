package br.com.omnidevs.gcsn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import br.com.omnidevs.gcsn.ui.screens.SplashScreen
import br.com.omnidevs.gcsn.ui.theme.AppTheme
import br.com.omnidevs.gcsn.util.AndroidMediaContentReader
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.ApplicationContext
import br.com.omnidevs.gcsn.util.SecureStorageProvider
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ApplicationContext.initialize(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val secureStorageProvider = SecureStorageProvider(applicationContext)
        val mediaContentReader = AndroidMediaContentReader()

        AppDependencies.initialize(
            secureStorageProvider = secureStorageProvider,
            mediaContentReaderProvider = mediaContentReader,
        )

        setContent {
            AppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Navigator(screen = SplashScreen()) { navigator ->
                            SlideTransition(navigator = navigator)
                        }
                    }
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        ApplicationContext.initialize(this)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    AppTheme {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                Navigator(screen = SplashScreen()) { navigator ->
                    SlideTransition(navigator = navigator)
                }
            }
        }
    }
}