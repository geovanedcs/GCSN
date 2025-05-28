package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.components.ProfileHeader
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch

class ProfileScreen(
    private val handle: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.current

        var actor by remember { mutableStateOf<Actor?>(null) }
        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoadingActor by remember { mutableStateOf(true) }
        var isLoadingFeed by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.ProfileTab) }

        val blueskyApi = remember { BlueskyApi() }

        // Add scroll behavior for TopAppBar
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

        val tabs = listOf(
            TabItem.HomeTab,
            TabItem.SearchTab,
            TabItem.ProfileTab
        )

        LaunchedEffect(handle) {
            loadProfileData(
                blueskyApi = blueskyApi,
                handle = handle,
                onStart = {
                    isLoadingActor = true
                    errorMessage = null
                },
                onComplete = {
                    isLoadingActor = false
                    isLoadingFeed = false
                },
                onActorLoaded = { loadedActor ->
                    actor = loadedActor
                    isLoadingFeed = true
                },
                onFeedLoaded = { userFeed ->
                    feed = userFeed
                },
                onError = { error ->
                    errorMessage = error
                }
            )
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Top)
                    ),
                    title = { Text(actor?.displayName ?: handle) },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                loadProfileData(
                                    blueskyApi = blueskyApi,
                                    handle = handle,
                                    onStart = {
                                        isLoadingActor = true
                                        errorMessage = null
                                    },
                                    onComplete = {
                                        isLoadingActor = false
                                        isLoadingFeed = false
                                    },
                                    onActorLoaded = { loadedActor ->
                                        actor = loadedActor
                                        isLoadingFeed = true
                                    },
                                    onFeedLoaded = { userFeed ->
                                        feed = userFeed
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            bottomBar = {
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
                                if (tab != currentTab) {
                                    when (tab) {
                                        TabItem.HomeTab -> {
                                            navigator?.push(HomeScreen())
                                        }

                                        TabItem.SearchTab -> {
                                            navigator?.push(SearchScreen())
                                        }

                                        TabItem.ProfileTab -> {
                                            // Already on profile tab
                                        }
                                    }
                                    currentTab = tab
                                }
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when {
                    isLoadingActor -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = errorMessage ?: "Erro desconhecido",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    actor != null -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ProfileHeader(actor = actor!!)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoadingFeed) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    feed?.let { userFeed ->
                                        items(userFeed.feed) { feedViewPost ->
                                            PostItem(post = feedViewPost.post)
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
}

private suspend fun loadProfileData(
    blueskyApi: BlueskyApi,
    handle: String,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onActorLoaded: (Actor) -> Unit,
    onFeedLoaded: (Feed) -> Unit,
    onError: (String) -> Unit
) {
    onStart()

    try {
        val actor = blueskyApi.getProfile(handle)
        onActorLoaded(actor)

        try {
            val userFeed = blueskyApi.getAuthorFeed(actor.did)
            onFeedLoaded(userFeed)
        } catch (e: Exception) {
            onError("Erro ao carregar feed: ${e.message}")
        }
    } catch (e: Exception) {
        onError("Erro ao carregar perfil: ${e.message}")
    } finally {
        onComplete()
    }
}