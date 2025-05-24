package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.components.ProfileHeader
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch

class ProfileScreen(
    val actor: Actor,
    val api: BlueskyApi,
) : Screen {

    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Perfil") },
                        navigationIcon = {
                            IconButton(onClick = { /* Ação de navegação, se necessário */ }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.Companion.padding(paddingValues)) {
                    LaunchedEffect(actor.did) {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                feed = api.getAuthorFeed(actor.did)
                            } catch (e: Exception) {
                                errorMessage = "Erro ao carregar o feed: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (errorMessage != null) {
                        Box(
                            modifier = Modifier.Companion.fillMaxSize(),
                            contentAlignment = Alignment.Companion.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Erro desconhecido",
                                color = MaterialTheme.colors.error
                            )
                        }
                    } else {
                        Column(modifier = Modifier.Companion.fillMaxSize()) {
                            ProfileHeader(actor = actor)
                            Spacer(modifier = Modifier.Companion.height(16.dp))
                            LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
                                feed?.let { posts ->
                                    items(posts.feed.size) { index ->
                                        PostItem(post = posts.feed[index].post)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}