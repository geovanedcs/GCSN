package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import gcsn.composeapp.generated.resources.Res
import gcsn.composeapp.generated.resources._logo
import gcsn.composeapp.generated.resources.logo
import gcsn.composeapp.generated.resources.logo_dark
import org.jetbrains.compose.resources.painterResource

class FirstStartScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val isDarkTheme = isSystemInDarkTheme()

        Scaffold(
            topBar = { TopAppBar(title = { Text("Bem-vindo!") }) }
        ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = if(isDarkTheme) {
                    painterResource(Res.drawable.logo_dark)
                } else {
                    painterResource(Res.drawable._logo)
                },
                contentDescription = "GCSN",
                modifier = Modifier.size(150.dp),


            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    navigator.push(LoginScreen())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navigator.push(LoginScreen()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cadastrar-se")
            }
        }
        }

    }
}
