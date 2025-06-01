package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.FeedItem
import br.com.omnidevs.gcsn.model.post.PostRef
import br.com.omnidevs.gcsn.model.post.ReplyRef
import br.com.omnidevs.gcsn.model.post.interactions.ThreadViewPost
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.ConfirmationDialog
import br.com.omnidevs.gcsn.ui.components.ConfirmationDialogType
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.util.PostInteractionHandler
import br.com.omnidevs.gcsn.util.SearchScreenWithQuery
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch


class ThreadScreen(private val postUri: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val blueskyApi = remember { BlueskyApi() }
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val listState = rememberLazyListState()

        var isLoading by remember { mutableStateOf(true) }
        var threadView by remember { mutableStateOf<ThreadViewPost?>(null) }
        var error by remember { mutableStateOf<String?>(null) }
        var replyText by remember { mutableStateOf("") }
        var isSubmitting by remember { mutableStateOf(false) }
        var isDeleting by remember { mutableStateOf(false) }

        // States for confirmation dialog
        var showConfirmationDialog by remember { mutableStateOf(false) }
        var dialogTitle by remember { mutableStateOf("") }
        var dialogMessage by remember { mutableStateOf("") }
        var dialogConfirmText by remember { mutableStateOf("") }
        var dialogType by remember { mutableStateOf(ConfirmationDialogType.NORMAL) }
        var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

        // Function to show the confirmation dialog
        val showDialog = { title: String, message: String, confirmText: String,
                           type: ConfirmationDialogType, action: () -> Unit ->
            dialogTitle = title
            dialogMessage = message
            dialogConfirmText = confirmText
            dialogType = type
            pendingAction = action
            showConfirmationDialog = true
        }

        // Handle post deletion
        val handleDeletePost = { postUri: String ->
            coroutineScope.launch {
                isDeleting = true
                try {
                    blueskyApi.deletePost(postUri)
                    snackbarHostState.showSnackbar("Publicação excluída com sucesso")

                    // If it was the main post, navigate back
                    if (postUri == ThreadScreen@ this@ThreadScreen.postUri) {
                        navigator.pop()
                    } else {
                        // Otherwise refresh the thread
                        threadView =
                            blueskyApi.getThreadView(ThreadScreen@ this@ThreadScreen.postUri)
                    }
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("Falha ao excluir publicação: ${e.message}")
                } finally {
                    isDeleting = false
                }
            }
        }

        LaunchedEffect(postUri) {
            try {
                isLoading = true
                threadView = blueskyApi.getThreadView(postUri)
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Conversa") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                // Reply input field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Escreva uma resposta...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        enabled = !isSubmitting
                    )
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                if (replyText.isBlank()) return@launch

                                isSubmitting = true
                                try {
                                    val rootPost = threadView?.thread?.post
                                    if (rootPost != null) {
                                        blueskyApi.createPost(
                                            text = replyText,
                                            images = emptyList(),
                                            reply = ReplyRef(
                                                root = PostRef(
                                                    uri = rootPost.uri,
                                                    cid = rootPost.cid
                                                ),
                                                parent = PostRef(
                                                    uri = rootPost.uri,
                                                    cid = rootPost.cid
                                                )
                                            )
                                        )
                                        replyText = ""
                                        threadView = blueskyApi.getThreadView(postUri)
                                        snackbarHostState.showSnackbar("Resposta enviada com sucesso!")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Falha ao enviar resposta: ${e.message}")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = replyText.isNotBlank() && !isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar resposta",
                            tint = if (replyText.isNotBlank() && !isSubmitting)
                                MaterialTheme.colorScheme.primary
                            else
                                Color.Gray
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    error != null -> {
                        Text(
                            text = "Erro ao carregar o thread: $error",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    threadView != null -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            content = {
                                // Parent post(s) if available
                                threadView?.thread?.parent?.let { parent ->
                                    item {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp)
                                            ) {
                                                PostItem(
                                                    feedItem = FeedItem(post = parent.post),
                                                    onAuthorClick = { authorDid ->
                                                        navigator.push(ProfileScreen(authorDid))
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
                                                            }
                                                        )
                                                    },
                                                    onParentClick = { parentUri ->
                                                        navigator.push(ThreadScreen(parentUri))
                                                    },
                                                    onTagClick = { tag ->
                                                        navigator.push(SearchScreenWithQuery("#$tag"))
                                                    },
                                                    onMentionClick = { did ->
                                                        navigator.push(ProfileScreen(did))
                                                    },
                                                    onLinkClick = { uri -> },
                                                    showConfirmationDialog = showDialog,
                                                    onDeleteClick = { postUri ->
                                                        handleDeletePost(
                                                            postUri
                                                        )
                                                    }
                                                )
                                            }
                                            ThreadConnector()
                                        }
                                    }
                                }

                                // Main post
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                    ) {
                                        PostItem(
                                            feedItem = FeedItem(post = threadView!!.thread.post),
                                            onAuthorClick = { authorDid ->
                                                navigator.push(ProfileScreen(authorDid))
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
                                                    }
                                                )
                                            },
                                            onParentClick = { parentUri ->
                                                navigator.push(ThreadScreen(parentUri))
                                            },
                                            onTagClick = { tag ->
                                                navigator.push(SearchScreenWithQuery("#$tag"))
                                            },
                                            onMentionClick = { did ->
                                                navigator.push(ProfileScreen(did))
                                            },
                                            onLinkClick = { uri -> },
                                            showConfirmationDialog = showDialog,
                                            onDeleteClick = { postUri -> handleDeletePost(postUri) }
                                        )
                                    }
                                }

                                // Replies
                                threadView?.thread?.replies?.forEachIndexed { index, reply ->
                                    item {
                                        ThreadConnector()
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 8.dp)
                                        ) {
                                            PostItem(
                                                feedItem = FeedItem(post = reply.post),
                                                onAuthorClick = { authorDid ->
                                                    navigator.push(ProfileScreen(authorDid))
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
                                                        }
                                                    )
                                                },
                                                onParentClick = { parentUri ->
                                                    navigator.push(ThreadScreen(parentUri))
                                                },
                                                onTagClick = { tag ->
                                                    navigator.push(SearchScreenWithQuery("#$tag"))
                                                },
                                                onMentionClick = { did ->
                                                    navigator.push(ProfileScreen(did))
                                                },
                                                onLinkClick = { uri -> },
                                                showConfirmationDialog = showDialog,
                                                onDeleteClick = { postUri ->
                                                    handleDeletePost(
                                                        postUri
                                                    )
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        )
                    }
                }

                if (isSubmitting || isDeleting) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
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

    @Composable
    private fun ThreadConnector() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(2.dp)
                    .align(Alignment.CenterStart)
                    .padding(start = 40.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    }
}