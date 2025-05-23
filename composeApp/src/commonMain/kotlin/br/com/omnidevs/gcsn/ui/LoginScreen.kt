package br.com.omnidevs.gcsn.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.AuthManager
import br.com.omnidevs.gcsn.network.api.AuthResponse
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.network.api.BlueskyAuthApi
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch


object LoginScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var authorization by remember { mutableStateOf<AuthResponse?>(null) }
        var actor by remember { mutableStateOf<Actor?>(null) }
        var feed by remember { mutableStateOf<Feed?>(null) }
        val authApi = BlueskyAuthApi()
        val api = BlueskyApi()
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        var showErrorDialog by remember { mutableStateOf(false) }
        var errorMessageForDialog by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                authorization = authApi.login(email, password)
                                if (!authorization?.accessJwt.isNullOrEmpty()) {
                                    AuthManager.accessToken = authorization?.accessJwt
                                } else {
                                    errorMessage = "Erro ao autenticar"
                                    errorMessageForDialog = authorization.toString()
                                    showErrorDialog = true
                                }
                                actor = api.getProfile(authorization?.handle.toString())
                                if (actor!!.handle.isNotEmpty()) {

                                    navigator.replaceAll(ProfileScreen(actor!!, api))
                                } else {
                                    errorMessage = "Erro ao obter feed"
                                    errorMessageForDialog = feed.toString()
                                    showErrorDialog = true
                                }
                                isLoading = false
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Erro ao autenticar"
                                errorMessageForDialog = e.message
                                showErrorDialog = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar")
                }
                Spacer(modifier = Modifier.height(16.dp))
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


