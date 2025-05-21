package br.com.omnidevs.gcsn.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.model.post.Post
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
        var posts by remember { mutableStateOf<Feed?>(null) }
        val authApi = BlueskyAuthApi()
        val api = BlueskyApi()
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()
        var logou by remember { mutableStateOf("") }

        suspend fun loginBluesky(): Boolean {
            isLoading = true
            try {
                val response = authApi.login(email, password)
                if (response?.accessJwt?.isNotEmpty() == true) {
                    authorization = response
                    AuthManager.accessToken = authorization?.accessJwt
                    return true
                } else {
                    return false
                }
            } catch (e: Exception) {
                errorMessage = "Erro: ${e.message}"
            } finally {
                isLoading = false
            }
            return false
        }

        suspend fun getProfile(email: String) {
            try {
                val response = api.getProfile(email)
                if (response != null) {
                    actor = response
                } else {
                    //TODO handle error
                }
            } catch (e: Exception) {
                errorMessage = "Erro: ${e.message}"
            } finally {
                isLoading = false
            }
        }

        suspend fun getFeed(actor: String) {
            try {
                val response = api.getAuthorFeed(actor, 20)
                if (response != null) {
                    posts = response
                } else {
                    //TODO handle error
                }
            } catch (e: Exception) {
                errorMessage = "Erro: ${e.message}"
            } finally {
                isLoading = false
            }
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
//                Text(text = password, color = MaterialTheme.colors.error)
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            var retorno = loginBluesky()
                            if (!retorno) {
                                errorMessage = "Erro ao autenticar"
                            }
                            if (authorization?.accessJwt?.isNotEmpty() == true) {
                                getProfile(authorization?.handle.toString())
//                                getFeed(authorization!!.email.toString())    // or .did, as required
                                if (actor != null) {
                                    navigator.replaceAll(ProfileScreen(actor!!))
                                } else {
                                    errorMessage = "Erro ao obter perfil ou feed"
                                }
                            } else {
                                errorMessage = "Erro ao autenticar"
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

