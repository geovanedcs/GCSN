package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.network.api.AuthResponse
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.network.api.BlueskyAuthApi
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch

class LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var authorization by remember { mutableStateOf<AuthResponse?>(null) }
        var actor by remember { mutableStateOf<Actor?>(null) }
        val authApi = BlueskyAuthApi()
        val api = BlueskyApi()
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        var showErrorDialog by remember { mutableStateOf(false) }
        var errorMessageForDialog by remember { mutableStateOf<String?>(null) }
        var authService = AppDependencies.authService

        fun triggerErrorDialog(message: String) {
            errorMessageForDialog = message
            showErrorDialog = true
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Login") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.Companion.fillMaxWidth()
                )
                Spacer(modifier = Modifier.Companion.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.Companion.height(16.dp))
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                authorization = authApi.login(email, password)
                                if (!authorization?.accessJwt.isNullOrEmpty()) {
                                    AuthManager.accessToken = authorization?.accessJwt
                                    AuthManager.refreshToken = authorization?.refreshJwt

                                    authService.saveUserData(
                                        did = authorization?.did.toString(),
                                        authToken = authorization?.accessJwt.toString(),
                                        refreshToken = authorization?.refreshJwt.toString(),
                                        handle = authorization?.handle.toString()
                                    )
                                    actor = api.getProfile(authorization?.handle.toString())
                                    if (actor?.handle?.isNotEmpty() == true) {
                                        navigator.replaceAll(ProfileScreen(actor!!, api))
                                    } else {
                                        triggerErrorDialog("Erro ao obter perfil do usuário")
                                    }
                                } else {
                                    triggerErrorDialog("Erro ao autenticar")
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                triggerErrorDialog(e.message ?: "Erro desconhecido")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLoading) "Carregando..." else "Entrar")
                }
                Spacer(modifier = Modifier.Companion.height(16.dp))
            }
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showErrorDialog = false
                        errorMessageForDialog = null
                    },
                    title = { Text("Erro de Login") },
                    text = { Text(errorMessageForDialog ?: "Ocorreu um erro.") },
                    confirmButton = {
                        Button(onClick = {
                            showErrorDialog = false
                            errorMessageForDialog = null
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}