package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberScaffoldState
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
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.PostItem
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()

        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }

        val blueskyApi = remember { BlueskyApi() }

        LaunchedEffect(Unit) {
            loadFeed(blueskyApi, onSuccess = { feed = it }, onError = { error = it })
            isLoading = false
        }

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                error = null
                                loadFeed(
                                    blueskyApi,
                                    onSuccess = { feed = it },
                                    onError = { error = it }
                                )
                                isLoading = false
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when {
                    isLoading && feed == null -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    error != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Error loading feed: $error")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    error = null
                                    loadFeed(
                                        blueskyApi,
                                        onSuccess = { feed = it },
                                        onError = { error = it }
                                    )
                                    isLoading = false
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }

                    feed != null -> {
                        LazyColumn {
                            items(feed!!.feed) { feedViewPost ->
                                PostItem(post = feedViewPost.post)
                            }

                            item {
                                if (feed?.cursor != null) {
                                    LoadMoreButton(
                                        isLoading = isLoading,
                                        onClick = {
                                            coroutineScope.launch {
                                                isLoading = true
                                                try {
                                                    val nextPage =
                                                        blueskyApi.getFeed(
                                                            feed = "at://did:plc:jzecvjo2bsjptyxnfoixfnfv/app.bsky.feed.generator/aaalrki4j7sjw",
                                                            limit = 20,
                                                            cursor = feed?.cursor
                                                        )
                                                    feed = Feed(
                                                        feed = feed!!.feed + nextPage.feed,
                                                        cursor = nextPage.cursor
                                                    )
                                                } catch (e: Exception) {
                                                    scaffoldState.snackbarHostState.showSnackbar(
                                                        "Failed to load more posts: ${e.message}"
                                                    )
                                                    println(e.message)
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadFeed(
        api: BlueskyApi,
        onSuccess: (Feed) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val feed =
                api.getFeed(feed = "at://did:plc:jzecvjo2bsjptyxnfoixfnfv/app.bsky.feed.generator/aaalrki4j7sjw",
                    limit = 20)
            onSuccess(feed)
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
        }
    }
}

@Composable
private fun LoadMoreButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = onClick) {
                Text("Load more")
            }
        }
    }
}