package br.com.omnidevs.gcsn.ui.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.ui.components.CommonBottomBar
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.util.AppDependencies
import br.com.omnidevs.gcsn.util.AuthService
import br.com.omnidevs.gcsn.util.AuthState
import br.com.omnidevs.gcsn.util.AuthStateManager
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch

class SearchScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.current
        val snackbarHostState = remember { SnackbarHostState() }
        val authService = AppDependencies.authService
        var isLoading by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.SearchTab) }

        // Add scroll behavior for TopAppBar
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
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Buscar") },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    performSearch(
                                        query = searchQuery,
                                        authService = authService,
                                        navigator = navigator,
                                        onSearchStart = { isLoading = true },
                                        onSearchComplete = { isLoading = false },
                                        onError = { error ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Erro: $error")
                                            }
                                        }
                                    )
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
                                performSearch(
                                    query = searchQuery,
                                    authService = authService,
                                    navigator = navigator,
                                    onSearchStart = { isLoading = true },
                                    onSearchComplete = { isLoading = false },
                                    onError = { error ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Erro: $error")
                                        }
                                    }
                                )
                            }
                        })
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Área onde os resultados da busca serão mostrados
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        // Estado vazio inicial
                        Text(
                            "Digite algo para buscar",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }

    private fun performSearch(
        query: String,
        authService: AuthService,
        navigator: Navigator?,
        onSearchStart: () -> Unit,
        onSearchComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        onSearchStart()
        // Aqui implementaremos a lógica de busca posteriormente
        println("Buscando por: $query")
        onSearchComplete()
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