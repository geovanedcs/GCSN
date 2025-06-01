package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.Feed
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.components.CommonBottomBar
import br.com.omnidevs.gcsn.ui.components.PostItem
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.ui.screens.ProfileScreen
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.AuthService
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import coil3.compose.AsyncImage
import gcsn.composeapp.generated.resources.Res
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchScreen(private val initialQuery: String = "") : Screen {
    enum class SearchMode { POSTS, USERS }

    @OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        val authService = AppDependencies.authService
        val blueskyApi = BlueskyApi()

        var isLoading by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf(initialQuery) }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.SearchTab) }

        var postResults by remember { mutableStateOf<Feed?>(null) }
        var userResults by remember { mutableStateOf<List<Actor>>(emptyList()) }
        var searchCursor by remember { mutableStateOf<String?>(null) }
        var searchMode by remember { mutableStateOf(SearchMode.POSTS) }

        val searchQueryFlow = remember { MutableStateFlow("") }

        LaunchedEffect(initialQuery) {
            if (initialQuery.isNotEmpty()) {
                searchQueryFlow.value = initialQuery
            }
        }

        LaunchedEffect(searchQueryFlow) {
            searchQueryFlow
                .debounce(600)
                .collect { query ->
                    if (query.isNotEmpty() && query.length >= 2) {
                        isLoading = true
                        try {
                            when (searchMode) {
                                SearchMode.POSTS -> {
                                    val searchResults = blueskyApi.searchPosts(query)
                                    postResults = blueskyApi.convertSearchPostsToFeed(searchResults)
                                    searchCursor = searchResults.cursor
                                }
                                SearchMode.USERS -> {
                                    val results = blueskyApi.searchActors(query)
                                    userResults = results.actors
                                    searchCursor = results.cursor
                                }
                            }
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Erro: ${e.message}")
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                }
        }

        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        val bottomBarState = remember { derivedStateOf { scrollBehavior.state.heightOffset == 0f } }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Top)
                    ),
                    title = { Text("Busca") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
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
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            searchQueryFlow.value = it
                        },
                        label = { Text("Buscar") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQueryFlow.value = searchQuery
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                searchQueryFlow.value = searchQuery
                            }
                        })
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = searchMode == SearchMode.POSTS,
                            onClick = {
                                searchMode = SearchMode.POSTS
                                if (searchQuery.isNotEmpty()) {
                                    searchQueryFlow.value = searchQuery
                                }
                            },
                            label = { Text("Posts") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = "Posts e Hashtags",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )

                        FilterChip(
                            selected = searchMode == SearchMode.USERS,
                            onClick = {
                                searchMode = SearchMode.USERS
                                if (searchQuery.isNotEmpty()) {
                                    searchQueryFlow.value = searchQuery
                                }
                            },
                            label = { Text("Pessoas") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Pessoas",
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (searchQuery.isEmpty()) {
                        Text(
                            "Digite algo para buscar",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        when (searchMode) {
                            SearchMode.POSTS -> {
                                if (postResults == null || postResults?.feed?.isEmpty() == true) {
                                    Text(
                                        "Nenhum post encontrado",
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(postResults?.feed ?: emptyList()) { feedItem ->
                                            PostItem(
                                                feedItem = feedItem,
                                                onAuthorClick = { did ->
                                                    navigator?.push(ProfileScreen(did))
                                                },
                                                onParentClick = { uri ->
                                                    // Handle opening thread if needed
                                                },
                                                onTagClick = { tag ->
                                                    // Update search for the selected tag
                                                    searchQuery = "#$tag"
                                                    searchQueryFlow.value = searchQuery
                                                },
                                                onMentionClick = { did ->
                                                    // Navigate to mentioned user's profile
                                                    navigator?.push(ProfileScreen(did))
                                                },
                                                onLinkClick = { uri ->
                                                    // Handle link click if needed
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            SearchMode.USERS -> {
                                if (userResults.isEmpty()) {
                                    Text(
                                        "Nenhum usuário encontrado",
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                } else {
                                    LazyColumn(
                                        contentPadding = PaddingValues(vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(userResults) { actor ->
                                            UserListItem(
                                                actor = actor,
                                                onClick = {
                                                    navigator?.push(ProfileScreen(actor.did))
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
        }
    }
}

@Composable
fun UserListItem(actor: Actor, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(
                text = actor.displayName ?: actor.handle,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = "@${actor.handle}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            AsyncImage(
                model = actor.avatar ?: Res.getUri("drawable/avatar"),
                contentDescription = "Avatar de ${actor.displayName ?: actor.handle}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentScale = ContentScale.Crop,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
}