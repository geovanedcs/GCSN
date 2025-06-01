package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.*
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.util.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
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
        val authService = AppDependencies.authService
        val listState = rememberLazyListState()

        // Dialog state
        var showConfirmationDialog by remember { mutableStateOf(false) }
        var dialogTitle by remember { mutableStateOf("") }
        var dialogMessage by remember { mutableStateOf("") }
        var dialogConfirmText by remember { mutableStateOf("") }
        var dialogType by remember { mutableStateOf(ConfirmationDialogType.NORMAL) }
        var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

        // Function to show the dialog
        val showDialog = { title: String, message: String, confirmText: String,
                           type: ConfirmationDialogType, action: () -> Unit ->
            dialogTitle = title
            dialogMessage = message
            dialogConfirmText = confirmText
            dialogType = type
            pendingAction = action
            showConfirmationDialog = true
        }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomBarState = remember { derivedStateOf { scrollBehavior.state.heightOffset == 0f } }

        LaunchedEffect(Unit) {
            if (!validateAuthentication(authService, navigator)) {
                return@LaunchedEffect
            }
            loadFeed(blueskyApi, onSuccess = { feed = it }, onError = { error = it })
            isLoading = false
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Home") },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (!validateAuthentication(authService, navigator)) {
                                    return@launch
                                }
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
                            Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(),
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Top)
                    )
                )
            },
            bottomBar = {
                CommonBottomBar(
                    isVisible = bottomBarState,
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    navigator = navigator,
                    authService = authService
                )
            },
            floatingActionButton = {
                CommonFAB(
                    isVisible = bottomBarState,
                    icon = Icons.Default.Add,
                    contentDescription = "Nova postagem"
                ) {
                    if (validateAuthentication(authService, navigator)) {
                        navigator?.push(CreatePostScreen())
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
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
                            Text("Erro ao carregar feed: $error")
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
                                Text("Tentar novamente")
                            }
                        }
                    }

                    feed != null -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(feed!!.feed) { feedViewPost ->
                                PostItem(
                                    feedItem = feedViewPost,
                                    onAuthorClick = { authorDid ->
                                        navigator?.push(ProfileScreen(authorDid))
                                    },
                                    onLikeClick = { post, isLiking, onUpdate ->
                                        PostInteractionHandler.handleLikeAction(
                                            post = post,
                                            isLiking = isLiking,
                                            api = blueskyApi,
                                            scope = coroutineScope,
                                            snackbarHostState = snackbarHostState,
                                            showDialog = showDialog,
                                            onActionComplete = { updatedPost ->
                                                onUpdate(updatedPost)
                                                feed = feed?.copy(
                                                    feed = feed?.feed?.map { feedItem ->
                                                        if (feedItem.post.uri == updatedPost.uri) {
                                                            feedItem.copy(post = updatedPost)
                                                        } else {
                                                            feedItem
                                                        }
                                                    } ?: listOf()
                                                )
                                            }
                                        )
                                    },
                                    onRepostClick = { post, isReposting, onUpdate ->
                                        PostInteractionHandler.handleRepostAction(
                                            post = post,
                                            isReposting = isReposting,
                                            api = blueskyApi,
                                            scope = coroutineScope,
                                            snackbarHostState = snackbarHostState,
                                            showDialog = showDialog,
                                            onActionComplete = { updatedPost ->
                                                onUpdate(updatedPost)
                                                feed = feed?.copy(
                                                    feed = feed?.feed?.map { feedItem ->
                                                        if (feedItem.post.uri == updatedPost.uri) {
                                                            feedItem.copy(post = updatedPost)
                                                        } else {
                                                            feedItem
                                                        }
                                                    } ?: listOf()
                                                )
                                            }
                                        )
                                    },
                                    onParentClick = { parentUri ->
                                        navigator?.push(ThreadScreen(parentUri))
                                    },
                                    onTagClick = { tag ->
                                        navigator?.push(SearchScreenWithQuery("#$tag"))
                                    },
                                    onMentionClick = { did ->
                                        navigator?.push(ProfileScreen(did))
                                    },
                                    onLinkClick = { uri -> },
                                    showConfirmationDialog = showDialog
                                )
                            }

                            item {
                                if (isLoading && feed != null) {
                                    LoadingIndicator()
                                }
                            }
                        }

                        InfiniteScrollHandler(
                            listState = listState,
                            loading = isLoading,
                            onLoadMore = {
                                if (feed?.cursor != null) {
                                    coroutineScope.launch {
                                        if (!validateAuthentication(authService, navigator)) {
                                            return@launch
                                        }
                                        isLoading = true
                                        try {
                                            val nextPage =
                                                blueskyApi.getTimeline(cursor = feed?.cursor)
                                            feed = feed?.copy(
                                                feed = feed?.feed.orEmpty() + nextPage.feed,
                                                cursor = nextPage.cursor
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Erro ao carregar mais posts: ${e.message}")
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                if (showConfirmationDialog) {
                    ConfirmationDialog(
                        title = dialogTitle,
                        message = dialogMessage,
                        confirmButtonText = dialogConfirmText,
                        onConfirm = {
                            pendingAction?.invoke()
                            showConfirmationDialog = false
                        },
                        onDismiss = { showConfirmationDialog = false },
                        type = dialogType
                    )
                }
            }
        }
    }

    private suspend fun validateAuthentication(
        authService: AuthService,
        navigator: Navigator?
    ): Boolean {
        val isValid = authService.validateToken()
        if (!isValid) {
            AuthStateManager.setAuthState(AuthState.LOGGED_OUT)
            navigator?.replaceAll(FirstStartScreen())
            return false
        }
        return true
    }

    private suspend fun loadFeed(
        api: BlueskyApi,
        onSuccess: (Feed) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val feed = api.getTimeline()
            onSuccess(feed)
        } catch (e: Exception) {
            if (e.message?.contains("unauthorized", ignoreCase = true) == true) {
                AuthStateManager.setAuthState(AuthState.LOGGED_OUT)
            }
            onError(e.message ?: "Erro desconhecido")
        }
    }
}