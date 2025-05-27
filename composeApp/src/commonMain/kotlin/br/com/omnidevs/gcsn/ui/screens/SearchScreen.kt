package br.com.omnidevs.gcsn.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.ui.navigation.HideOnScrollBottomBar
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import kotlinx.coroutines.launch

class SearchScreen : Screen {
    @Composable
    override fun Content() {
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState()
        val navigator = LocalNavigator.current
        val authService = AppDependencies.authService
        val userData = remember { authService.getUserData() }
        var isLoading by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var currentTab by remember { mutableStateOf<TabItem>(TabItem.SearchTab) }

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

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Busca") },
                    navigationIcon = {
                        IconButton(onClick = { navigator?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
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
                                    navigator?.pop() // Volta para a HomeScreen
                                }
                                TabItem.SearchTab -> {
                                    // Já estamos na SearchScreen
                                }
                                TabItem.ProfileTab -> {
                                    navigator?.push(ProfileScreen(handle = userData!!.handle))
                                }
                            }
                            currentTab = selectedTab
                        }
                    },
                    scrollConnection = scrollConnection
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .nestedScroll(scrollConnection)
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
                                    performSearch(searchQuery)
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Buscar")
                            }
                        },
                        modifier = Modifier.fillMaxSize(0.9f),
                        maxLines = 1,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            if (searchQuery.isNotEmpty()) {
                                performSearch(searchQuery)
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

    private fun performSearch(query: String) {
        // Implementação da busca (será adicionada posteriormente)
        println("Buscando por: $query")
    }
}