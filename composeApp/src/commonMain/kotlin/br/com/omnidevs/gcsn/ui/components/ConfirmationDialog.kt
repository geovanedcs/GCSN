package br.com.omnidevs.gcsn.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class ConfirmationDialogType {
    WARNING, // For destructive actions (delete, unfollow)
    NORMAL   // For regular confirmations
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmButtonText: String = "Confirmar",
    dismissButtonText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    type: ConfirmationDialogType = ConfirmationDialogType.NORMAL,
    icon: ImageVector? = if (type == ConfirmationDialogType.WARNING) Icons.Default.Warning else null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = when (type) {
                            ConfirmationDialogType.WARNING -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(text = title)
            }
        },
        text = { Text(text = message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = when (type) {
                    ConfirmationDialogType.WARNING -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                    else -> ButtonDefaults.buttonColors()
                }
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = dismissButtonText)
            }
        }
    )
}