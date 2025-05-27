package br.com.omnidevs.gcsn.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.dp

@Composable
fun HideOnScrollBottomBar(
    tabs: List<TabItem>,
    currentTab: TabItem,
    onTabSelected: (TabItem) -> Unit,
    scrollConnection: NestedScrollConnection
) {
    var isVisible by remember { mutableStateOf(true) }
    val connectionWithVisibility = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10) {
                    isVisible = false
                } else if (available.y > 10) {
                    isVisible = true
                }

                return scrollConnection.onPreScroll(available, source)
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                return scrollConnection.onPostScroll(consumed, available, source)
            }
        }
    }

    val barOffset by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 80.dp,
        label = "NavBarOffset"
    )

    NavigationBar(
        modifier = Modifier.offset(y = barOffset),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    tab.options.icon?.let { icon ->
                        Icon(
                            painter = icon,
                            contentDescription = tab.options.title
                        )
                    }
                },
                label = { Text(tab.options.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}