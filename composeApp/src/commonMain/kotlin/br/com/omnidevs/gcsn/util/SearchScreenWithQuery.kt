package br.com.omnidevs.gcsn.util

import androidx.compose.runtime.Composable
import br.com.omnidevs.gcsn.ui.screens.SearchScreen
import cafe.adriel.voyager.core.screen.Screen

// Helper screen to support passing initial query to SearchScreen
class SearchScreenWithQuery(private val initialQuery: String) : Screen {
    @Composable
    override fun Content() {
        SearchScreen(initialQuery = initialQuery).Content()
    }
}