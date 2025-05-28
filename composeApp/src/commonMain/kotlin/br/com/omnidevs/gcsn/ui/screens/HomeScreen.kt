package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch

class HomeScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val navigator = LocalNavigator.current
        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.HomeTab) }
        val blueskyApi = remember { BlueskyApi() }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomBarState = remember { derivedStateOf { scrollBehavior.state.heightOffset == 0f } }

        val tabs = listOf(
            TabItem.HomeTab,
            TabItem.SearchTab,
            TabItem.ProfileTab
        )

        LaunchedEffect(Unit) {
            loadFeed(blueskyApi, onSuccess = { feed = it }, onError = { error = it })
            isLoading = false
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    scrollBehavior = scrollBehavior,
                )
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = bottomBarState.value,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                )  {
                    NavigationBar {
                        tabs.forEach { tab ->
                            NavigationBarItem(
                                icon = {
                                    tab.options.icon?.let { icon ->
                                        Icon(painter = icon, contentDescription = tab.options.title)
                                    }
                                },
                                label = { Text(tab.options.title) },
                                selected = currentTab == tab,
                                onClick = {
                                    currentTab = tab
                                    when (tab) {
                                        TabItem.HomeTab -> {
                                        }

                                        TabItem.SearchTab -> {
                                            navigator?.push(SearchScreen())
                                        }

                                        TabItem.ProfileTab -> {
                                            val userData = AppDependencies.authService.getUserData()
                                            navigator?.push(ProfileScreen(userData!!.handle))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
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
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                                    snackbarHostState.showSnackbar(
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
                api.getFeed(
                    feed = "at://did:plc:jzecvjo2bsjptyxnfoixfnfv/app.bsky.feed.generator/aaalrki4j7sjw",
                    limit = 20
                )
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