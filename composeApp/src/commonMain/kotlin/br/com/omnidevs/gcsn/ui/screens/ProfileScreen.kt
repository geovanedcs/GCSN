package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.components.ProfileHeader
import br.com.omnidevs.gcsn.ui.navigation.HideOnScrollBottomBar
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch

class ProfileScreen(
    private val handle: String
) : Screen {

    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()
        val navigator = LocalNavigator.current

        var actor by remember { mutableStateOf<Actor?>(null) }
        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoadingActor by remember { mutableStateOf(true) }
        var isLoadingFeed by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.ProfileTab) }

        val blueskyApi = remember { BlueskyApi() }

        val scrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    return Offset.Zero
                }
            }
        }

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

        MaterialTheme {
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        title = { Text(actor?.displayName ?: handle) },
                        navigationIcon = {
                            IconButton(onClick = { navigator?.pop() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
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
                        }
                    )
                },
                bottomBar = {
                    HideOnScrollBottomBar(
                        tabs = tabs,
                        currentTab = currentTab,
                        onTabSelected = { selectedTab ->
                            if (selectedTab != currentTab) {
                                when (selectedTab) {
                                    TabItem.HomeTab -> {
                                        navigator?.pop() // Volta para a tela anterior
                                    }

                                    TabItem.SearchTab -> {
                                        navigator?.push(SearchScreen())
                                    }

                                    TabItem.ProfileTab -> {
                                    }
                                }
                                currentTab = selectedTab
                            }
                        },
                        scrollConnection = scrollConnection
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                        .nestedScroll(scrollConnection)
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
                                    color = MaterialTheme.colors.error
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