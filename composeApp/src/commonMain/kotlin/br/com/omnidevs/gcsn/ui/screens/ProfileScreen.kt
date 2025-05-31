package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import br.com.omnidevs.gcsn.model.post.PostOrBlockedPost.Post
import br.com.omnidevs.gcsn.model.post.Viewer
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.CommonBottomBar
import br.com.omnidevs.gcsn.ui.components.CommonFAB
import br.com.omnidevs.gcsn.ui.components.ConfirmationDialog
import br.com.omnidevs.gcsn.ui.components.ConfirmationDialogType
import br.com.omnidevs.gcsn.ui.components.InfiniteScrollHandler
import br.com.omnidevs.gcsn.ui.components.LoadingIndicator
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.components.ProfileHeader
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.AuthService
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch

class ProfileScreen(
    private val handle: String
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.current
        val authService = AppDependencies.authService
        val snackbarHostState = remember { SnackbarHostState() }

        var actor by remember { mutableStateOf<Actor?>(null) }
        var feed by remember { mutableStateOf<Feed?>(null) }
        var isLoading by remember { mutableStateOf(true) }
        var isFollowLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.ProfileTab) }
        var isOwnProfile by remember { mutableStateOf(false) }

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

        // Estado para controlar a visibilidade do header
        val lazyListState = rememberLazyListState()
        val isHeaderVisible by remember {
            derivedStateOf {
                lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset < 200
            }
        }

        val blueskyApi = remember { BlueskyApi() }

        // Add scroll behavior para TopAppBar e estados para animação
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomBarState = remember { derivedStateOf { scrollBehavior.state.heightOffset == 0f } }

        LaunchedEffect(handle) {
            // Validar autenticação antes de carregar o perfil
            if (!validateAuthentication(authService, navigator)) {
                return@LaunchedEffect
            }

            // Verificar se é o próprio perfil do usuário
            val userData = authService.getUserData()
            isOwnProfile = userData?.handle == handle

            loadProfileData(
                blueskyApi = blueskyApi,
                handle = handle,
                onActorLoaded = { loadedActor -> actor = loadedActor },
                onFeedLoaded = { userFeed -> feed = userFeed },
                onError = { error -> errorMessage = error },
                onComplete = { isLoading = false }
            )
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                // Validar autenticação antes de recarregar os dados
                                if (!validateAuthentication(authService, navigator)) {
                                    return@launch
                                }

                                isLoading = true
                                errorMessage = null
                                loadProfileData(
                                    blueskyApi = blueskyApi,
                                    handle = handle,
                                    onActorLoaded = { loadedActor -> actor = loadedActor },
                                    onFeedLoaded = { userFeed -> feed = userFeed },
                                    onError = { error -> errorMessage = error },
                                    onComplete = { isLoading = false }
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
                CommonBottomBar(
                    isVisible = bottomBarState,
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    navigator = navigator,
                    authService = authService
                )
            },
            floatingActionButton = {
                // Mostrar FAB apenas se for o perfil do próprio usuário
                if (isOwnProfile) {
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
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                when {
                    isLoading && actor == null -> {
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
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                AnimatedVisibility(
                                    visible = isHeaderVisible,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        ProfileHeader(
                                            actor = actor!!,
                                            isOwnProfile = isOwnProfile,
                                            isLoading = isFollowLoading,
                                            onFollowClick = { shouldFollow ->
                                                // Fixed unfollow action
                                                coroutineScope.launch {
                                                    isFollowLoading = true
                                                    try {
                                                        if (shouldFollow) {
                                                            // Follow the user
                                                            val followResponse =
                                                                blueskyApi.followUser(actor!!.did)
                                                            // Update actor with new follow state
                                                            actor = actor!!.copy(
                                                                viewer = actor!!.viewer?.copy(
                                                                    following = followResponse.uri
                                                                )
                                                                    ?: Viewer(following = followResponse.uri)
                                                            )
                                                        } else {
                                                            // Unfollow the user
                                                            actor!!.viewer?.following?.let { followingUri ->
                                                                blueskyApi.unfollowUser(followingUri)
                                                                actor = actor!!.copy(
                                                                    viewer = actor!!.viewer.copy(
                                                                        following = null
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        snackbarHostState.showSnackbar(
                                                            if (shouldFollow) "Falha ao seguir: ${e.message}"
                                                            else "Falha ao deixar de seguir: ${e.message}"
                                                        )
                                                    } finally {
                                                        isFollowLoading = false
                                                    }
                                                }
                                            },
                                            showConfirmationDialog = showDialog
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                }
                            }

                            feed?.let { userFeed ->
                                items(userFeed.feed) { feedViewPost ->
                                    PostItem(
                                        feedItem = feedViewPost,
                                        onAuthorClick = { authorDid ->
                                            navigator?.push(ProfileScreen(authorDid))
                                        },
                                        onLikeClick = { post, isLiking ->
                                            // Fixed unlike action
                                            coroutineScope.launch {
                                                try {
                                                    if (isLiking) {
                                                        // Like the post
                                                        blueskyApi.likePost(post.uri, post.cid)
                                                    } else {
                                                        // Unlike the post
                                                        post.viewer?.like?.let { likeUri ->
                                                            blueskyApi.unlikePost(likeUri)
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar(
                                                        if (isLiking) "Falha ao curtir: ${e.message}"
                                                        else "Falha ao remover curtida: ${e.message}"
                                                    )
                                                }
                                            }
                                        },
                                        onParentClick = { postUri ->
                                            navigator?.push(ThreadScreen(postUri))
                                        },
                                        showConfirmationDialog = showDialog
                                    )
                                }
                            }

                            // Indicador de carregamento no final da lista
                            item {
                                if (feed?.cursor != null && isLoading) {
                                    LoadingIndicator()
                                }
                            }
                        }

                        // Handler para carregamento infinito
                        InfiniteScrollHandler(
                            listState = lazyListState,
                            loading = isLoading,
                            onLoadMore = {
                                if (feed?.cursor != null && actor != null) {
                                    coroutineScope.launch {
                                        if (!validateAuthentication(authService, navigator)) {
                                            return@launch
                                        }

                                        isLoading = true
                                        try {
                                            val nextPage = blueskyApi.getAuthorFeed(
                                                actor = actor!!.did,
                                                cursor = feed?.cursor
                                            )
                                            feed = Feed(
                                                feed = feed!!.feed + nextPage.feed,
                                                cursor = nextPage.cursor
                                            )
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(
                                                "Falha ao carregar mais posts: ${e.message}"
                                            )
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                // Display the confirmation dialog when needed
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

    // Função para validar autenticação
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
}

private suspend fun loadProfileData(
    blueskyApi: BlueskyApi,
    handle: String,
    onActorLoaded: (Actor) -> Unit,
    onFeedLoaded: (Feed) -> Unit,
    onError: (String) -> Unit,
    onComplete: () -> Unit
) {
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