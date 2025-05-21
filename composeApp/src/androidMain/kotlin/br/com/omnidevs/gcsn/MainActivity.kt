package br.com.omnidevs.gcsn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.com.omnidevs.gcsn.ui.FirstStartScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Navigator(screen = FirstStartScreen){
                SlideTransition(navigator = it)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    Navigator(screen = FirstStartScreen){
        SlideTransition(navigator = it)
    }
}