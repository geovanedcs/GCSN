package br.com.omnidevs.gcsn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.omnidevs.gcsn.network.api.BlueskyAuthApi
import br.com.omnidevs.gcsn.ui.HomeScreen
import br.com.omnidevs.gcsn.ui.LoginScreen
import br.com.omnidevs.gcsn.ui.RegisterUserScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = BlueskyAuthApi()

        setContent {
            LoginScreen(api = api){}
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val api = BlueskyAuthApi()
    LoginScreen(
        api = api,
    ) { }
}