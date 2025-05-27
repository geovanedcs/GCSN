package br.com.omnidevs.gcsn.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.model.actor.Actor
import br.com.omnidevs.gcsn.network.api.BlueskyApi
import br.com.omnidevs.gcsn.ui.screens.HomeScreen
import br.com.omnidevs.gcsn.ui.screens.ProfileScreen
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import kotlinx.coroutines.launch

sealed class TabItem(
    private val tabTitle: String,
    private val activeIcon: ImageVector,
    private val inactiveIcon: ImageVector,
    private val tabIndex: UInt
) : Tab {

    override val options: TabOptions
        @Composable
        get() = TabOptions(
            index = tabIndex.toUShort(),
            title = tabTitle,
            icon = rememberVectorPainter(
                if (this == currentTab) activeIcon else inactiveIcon
            )
        )

    companion object {
        internal var currentTab: TabItem = HomeTab
    }

    object HomeTab : TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home, 0u) {
        @Composable
        override fun Content() {
            currentTab = this
            HomeScreen()
        }
    }

    object SearchTab : TabItem("Buscar", Icons.Filled.Search, Icons.Outlined.Search, 1u) {
        @Composable
        override fun Content() {
            currentTab = this
            PlaceholderScreen("Tela de Busca em Desenvolvimento")
        }
    }

    object ProfileTab : TabItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person, 2u) {
        @Composable
        override fun Content() {
            currentTab = this

            // Estados necessários
            var actor by remember { mutableStateOf<Actor?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()
            val api = BlueskyApi()

            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        val userData = AppDependencies.authService.getUserData()
                        if (userData != null) {
                            val profile = api.getProfile(userData.handle)
                            actor = profile
                        } else {
                            errorMessage = "Usuário não autenticado"
                        }
                    } catch (e: Exception) {
                        errorMessage = "Erro ao carregar perfil: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                actor != null -> {
                    ProfileScreen(actor = actor!!, api = api)
                }

                else -> {
                    PlaceholderScreen(errorMessage ?: "Não foi possível carregar o perfil")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.material3.Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}