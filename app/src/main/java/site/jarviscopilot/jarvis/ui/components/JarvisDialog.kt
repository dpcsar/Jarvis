package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// A customized Dialog component that uses the Jarvis theme colors
@Composable
fun JarvisDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
    buttons: @Composable RowScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = JarvisTheme.colorScheme.surface,
                contentColor = JarvisTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = JarvisTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = JarvisTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                    content()
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    buttons()
                }
            }
        }
    }
}

// A customized confirmation dialog that uses the Jarvis theme
@Composable
fun JarvisConfirmationDialog(
    title: String,
    message: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel"
) {
    JarvisDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        content = {
            Text(
                text = message,
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        },
        buttons = {
            JarvisOutlinedButton(
                onClick = onDismissClick
            ) {
                Text(text = dismissText)
            }
            Spacer(modifier = Modifier.width(8.dp))
            JarvisButton(
                onClick = onConfirmClick
            ) {
                Text(text = confirmText)
            }
        }
    )
}

