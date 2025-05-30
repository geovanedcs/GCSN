package br.com.omnidevs.gcsn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import br.com.omnidevs.gcsn.ui.navigation.TabItem
import br.com.omnidevs.gcsn.ui.screens.HomeScreen
import br.com.omnidevs.gcsn.ui.screens.ProfileScreen
import br.com.omnidevs.gcsn.ui.screens.SearchScreen
import br.com.omnidevs.gcsn.ui.screens.DiscoverScreen
import br.com.omnidevs.gcsn.util.AuthService
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.launch

@Composable
fun CommonBottomBar(
    isVisible: State<Boolean>,
    currentTab: TabItem,
    onTabSelected: (TabItem) -> Unit,
    navigator: Navigator?,
    authService: AuthService
) {
    val coroutineScope = rememberCoroutineScope()

    val tabs = listOf(
        TabItem.HomeTab,
        TabItem.DiscoverTab,
        TabItem.SearchTab,
        TabItem.ProfileTab
    )

    AnimatedVisibility(
        visible = isVisible.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        NavigationBar {
            tabs.forEach { tab ->
                NavigationBarItem(
                    icon = {
                        tab.options.icon?.let { icon ->
                            Icon(painter = icon, contentDescription = tab.options.title)
                        }
                    },
                    label = { Text(tab.options.title) },
                    selected = currentTab == tab,
                    onClick = {
                        onTabSelected(tab)
                        if (currentTab != tab) {
                            when (tab) {
                                TabItem.HomeTab -> {
                                    navigator?.push(HomeScreen())
                                }
                                TabItem.DiscoverTab -> {
                                    navigator?.push(DiscoverScreen())
                                }
                                TabItem.SearchTab -> {
                                    navigator?.push(SearchScreen())
                                }
                                TabItem.ProfileTab -> {
                                    coroutineScope.launch {
                                        val userData = authService.getUserData()
                                        userData?.let {
                                            navigator?.push(ProfileScreen(it.handle))
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}