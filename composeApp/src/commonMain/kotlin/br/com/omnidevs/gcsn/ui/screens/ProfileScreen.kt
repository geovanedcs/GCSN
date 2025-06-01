package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
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
import br.com.omnidevs.gcsn.util.PostInteractionHandler
import br.com.omnidevs.gcsn.util.SearchScreenWithQuery
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
        var isDeleting by remember { mutableStateOf(false) } // Add state for delete operations
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.ProfileTab) }
        var isOwnProfile by remember { mutableStateOf(false) }

        val blueskyApi = remember { BlueskyApi() }

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

        // Function to handle post deletion
        val handleDeletePost = { postUri: String ->
            coroutineScope.launch {
                isDeleting = true
                try {
                    // Call API to delete post
                    blueskyApi.deletePost(postUri)

                    // Remove the post from the feed
                    feed = feed?.copy(
                        feed = feed?.feed?.filter { it.post.uri != postUri } ?: listOf()
                    )

                    snackbarHostState.showSnackbar("Publicação excluída com sucesso")
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Falha ao excluir publicação: ${e.message}")
                } finally {
                    isDeleting = false
                }
            }
        }

        // Estado para controlar a visibilidade do header
        val lazyListState = rememberLazyListState()
        val isHeaderVisible by remember {
            derivedStateOf {
                lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset < 200
            }
        }


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
                            // Profile header
                            item {
                                AnimatedVisibility(
                                    visible = isHeaderVisible || lazyListState.firstVisibleItemIndex == 0,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    actor?.let { currentActor ->
                                        ProfileHeader(
                                            actor = currentActor,
                                            isOwnProfile = isOwnProfile,
                                            isLoading = isFollowLoading,
                                            onFollowClick = { isCurrentlyFollowing ->
                                                coroutineScope.launch {
                                                    if (!validateAuthentication(
                                                            authService,
                                                            navigator
                                                        )
                                                    ) {
                                                        return@launch
                                                    }

                                                    isFollowLoading = true
                                                    try {
                                                        if (isCurrentlyFollowing) {
                                                            // IMPORTANT FIX: We need the follow URI, not just the DID
                                                            // Get the following URI from the viewer
                                                            val followingUri =
                                                                currentActor.viewer.following

                                                            if (followingUri != null) {
                                                                println("Unfollowing user with URI: $followingUri")
                                                                // Call unfollowUser with the actual follow URI
                                                                val success =
                                                                    blueskyApi.unfollowUser(
                                                                        followingUri
                                                                    )

                                                                if (success) {
                                                                    // Update local state
                                                                    val updatedViewer =
                                                                        currentActor.viewer.copy(
                                                                            following = null
                                                                        )

                                                                    actor =
                                                                        currentActor.copy(viewer = updatedViewer)
                                                                    snackbarHostState.showSnackbar(
                                                                        "Deixou de seguir ${currentActor.displayName ?: currentActor.handle}"
                                                                    )
                                                                } else {
                                                                    throw Exception("API returned unsuccessful result")
                                                                }
                                                            } else {
                                                                throw Exception("Follow URI não encontrado")
                                                            }
                                                        } else {
                                                            // Follow action
                                                            val result =
                                                                blueskyApi.followUser(currentActor.did)

                                                            // After following, we need to update the local state
                                                            // The URI format should be "at://{my_did}/app.bsky.graph.follow/{rkey}"
                                                            val followUri = result.uri

                                                            // Update local state with the new follow URI
                                                            val updatedViewer =
                                                                currentActor.viewer.copy(
                                                                    following = followUri
                                                                )

                                                            actor =
                                                                currentActor.copy(viewer = updatedViewer)
                                                            snackbarHostState.showSnackbar(
                                                                "Agora você segue ${currentActor.displayName ?: currentActor.handle}"
                                                            )
                                                        }
                                                    } catch (e: Exception) {
                                                        val action =
                                                            if (isCurrentlyFollowing) "deixar de seguir" else "seguir"
                                                        println("Error ${action}: ${e.message}")
                                                        e.printStackTrace()
                                                        snackbarHostState.showSnackbar("Falha ao $action: ${e.message}")

                                                        // Refresh profile data to ensure UI is consistent with server
                                                        loadProfileData(
                                                            blueskyApi = blueskyApi,
                                                            handle = handle,
                                                            onActorLoaded = { loadedActor ->
                                                                actor = loadedActor
                                                            },
                                                            onFeedLoaded = { userFeed ->
                                                                feed = userFeed
                                                            },
                                                            onError = { /* Ignore to avoid multiple errors */ },
                                                            onComplete = { /* No action needed */ }
                                                        )
                                                    } finally {
                                                        isFollowLoading = false
                                                    }
                                                }
                                            },
                                            showConfirmationDialog = showDialog
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Posts section
                            feed?.feed?.let { posts ->
                                items(posts) { feedViewPost ->
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
                                            // Navega para SearchScreen com a tag como query inicial
                                            navigator?.push(SearchScreenWithQuery("#$tag"))
                                        },
                                        onMentionClick = { did ->
                                            navigator?.push(ProfileScreen(did))
                                        },
                                        onLinkClick = { uri ->
                                            // Handle link click if needed
                                        },
                                        showConfirmationDialog = showDialog,
                                        onDeleteClick = { postUri -> handleDeletePost(postUri) }
                                    )
                                }
                            }

                            item {
                                if (isLoading && feed != null) {
                                    LoadingIndicator()
                                }
                            }
                        }

                        // InfiniteScrollHandler
                        InfiniteScrollHandler(
                            listState = lazyListState,
                            loading = isLoading,
                            onLoadMore = {
                                if (feed?.cursor != null) {
                                    coroutineScope.launch {
                                        if (!validateAuthentication(authService, navigator)) {
                                            return@launch
                                        }

                                        isLoading = true
                                        try {
                                            actor?.did?.let { actorDid ->
                                                val nextPage = blueskyApi.getAuthorFeed(
                                                    actor = actorDid,
                                                    cursor = feed?.cursor
                                                )
                                                feed = feed?.copy(
                                                    feed = feed?.feed.orEmpty() + nextPage.feed,
                                                    cursor = nextPage.cursor
                                                )
                                            }
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

                // Show loading overlay during deletion
                if (isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
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
            val userFeed = blueskyApi.getAuthorFeed(actor = actor.did)
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