package br.com.omnidevs.gcsn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

@Composable
fun CommonFAB(
    isVisible: State<Boolean>,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String = "Nova ação",
    onClick: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        FloatingActionButton(
            onClick = {
                coroutineScope.launch {
                    onClick()
                }
            },
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}