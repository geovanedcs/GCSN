package br.com.omnidevs.gcsn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.RegisterUserScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val api = BlueskyApi()

        setContent {
            RegisterUserScreen(api = api)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    val api = BlueskyApi()
    RegisterUserScreen(api = api)
}