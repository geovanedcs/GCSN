package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun InfiniteScrollHandler(
    listState: LazyListState,
    buffer: Int = 3, // Número de itens antes do final para acionar o carregamento
    loading: Boolean = false, // Para evitar múltiplas chamadas enquanto carrega
    onLoadMore: suspend () -> Unit
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = listState.layoutInfo.totalItemsCount

            // Carrega mais quando chegar perto do final
            lastVisibleItem >= totalItemsCount - buffer && !loading && totalItemsCount > 0
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .distinctUntilChanged()
            .collect {
                if (it) {
                    onLoadMore()
                }
            }
    }
}