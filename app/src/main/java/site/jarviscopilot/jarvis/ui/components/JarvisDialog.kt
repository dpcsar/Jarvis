package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
                containerColor = MaterialTheme.colorScheme.surface
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
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
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

@Preview(
    name = "JarvisDialog Light Theme",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisDialogPreviewLight() {
    JarvisTheme(darkTheme = false) {
        JarvisDialog(
            title = "Light Theme Dialog",
            onDismissRequest = { },
            content = {
                Text(
                    "This is a preview of the JarvisDialog component in light theme.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            buttons = {
                JarvisOutlinedButton(onClick = { }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                JarvisButton(onClick = { }) {
                    Text("Confirm")
                }
            }
        )
    }
}

@Preview(
    name = "JarvisDialog Dark Theme",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisDialogPreviewDark() {
    JarvisTheme(darkTheme = true) {
        JarvisDialog(
            title = "Dark Theme Dialog",
            onDismissRequest = { },
            content = {
                Text(
                    "This is a preview of the JarvisDialog component in dark theme.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            buttons = {
                JarvisOutlinedButton(onClick = { }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))
                JarvisButton(onClick = { }) {
                    Text("Confirm")
                }
            }
        )
    }
}

@Preview(
    name = "JarvisConfirmationDialog Light Theme",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisConfirmationDialogPreviewLight() {
    JarvisTheme(darkTheme = false) {
        JarvisConfirmationDialog(
            title = "Light Theme Confirmation",
            message = "This is a preview of the JarvisConfirmationDialog in light theme.",
            onConfirmClick = { },
            onDismissClick = { },
            onDismissRequest = { },
            confirmText = "Confirm",
            dismissText = "Cancel"
        )
    }
}

@Preview(
    name = "JarvisConfirmationDialog Dark Theme",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisConfirmationDialogPreviewDark() {
    JarvisTheme(darkTheme = true) {
        JarvisConfirmationDialog(
            title = "Dark Theme Confirmation",
            message = "This is a preview of the JarvisConfirmationDialog in dark theme.",
            onConfirmClick = { },
            onDismissClick = { },
            onDismissRequest = { },
            confirmText = "Confirm",
            dismissText = "Cancel"
        )
    }
}

