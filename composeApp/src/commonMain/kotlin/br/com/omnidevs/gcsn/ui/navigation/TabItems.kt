package br.com.omnidevs.gcsn.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import br.com.omnidevs.gcsn.ui.screens.DiscoverScreen
import br.com.omnidevs.gcsn.ui.screens.HomeScreen
import br.com.omnidevs.gcsn.ui.screens.ProfileScreen
import br.com.omnidevs.gcsn.ui.screens.SearchScreen
import br.com.omnidevs.gcsn.util.AppDependencies
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

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

    object DiscoverTab : TabItem("Descobrir", Icons.Filled.Explore, Icons.Outlined.Explore, 1u) {
        @Composable
        override fun Content() {
            currentTab = this
            DiscoverScreen()
        }
    }

    object SearchTab : TabItem("Buscar", Icons.Filled.Search, Icons.Outlined.Search, 2u) {
        @Composable
        override fun Content() {
            currentTab = this
            SearchScreen()
        }
    }

    object ProfileTab : TabItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person, 3u) {
        @Composable
        override fun Content() {
            currentTab = this

            val authService = AppDependencies.authService
            val userData = remember { authService.getUserData() }

            if (userData != null) {
                ProfileScreen(handle = userData.handle)
            } else {
                PlaceholderScreen("Usuário não autenticado")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(message: String) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp)
        )
    }
}