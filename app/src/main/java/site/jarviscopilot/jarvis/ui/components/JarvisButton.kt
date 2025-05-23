package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// A customized Button component that uses the Jarvis theme colors
@Composable
fun JarvisButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 88.dp, minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding,
        content = content
    )
}

// A customized OutlinedButton component that uses the Jarvis theme colors
@Composable
fun JarvisOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 88.dp, minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding,
        content = content
    )
}

// A button with both text and icon that uses the Jarvis theme
@Composable
fun JarvisIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: Color = MaterialTheme.colorScheme.onPrimary
) {
    JarvisButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )
        Text(
            text = text,
            modifier = Modifier.defaultMinSize(minWidth = 64.dp)
        )
    }
}

// Preview of all Jarvis button variants in light theme
@Preview(name = "Light Theme", showBackground = true, apiLevel = 35)
@Composable
fun JarvisButtonLightPreview() {
    JarvisTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            JarvisButton(
                onClick = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Standard Button")
            }

            JarvisOutlinedButton(
                onClick = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Outlined Button")
            }

            JarvisIconButton(
                text = "Icon Button",
                icon = Icons.Default.Add,
                onClick = {},
            )
        }
    }
}

// Preview of all Jarvis button variants in dark theme
@Preview(name = "Dark Theme", showBackground = true, apiLevel = 35, backgroundColor = 0xFF121212)
@Composable
fun JarvisButtonDarkPreview() {
    JarvisTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            JarvisButton(
                onClick = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Standard Button")
            }

            JarvisOutlinedButton(
                onClick = {},
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Outlined Button")
            }

            JarvisIconButton(
                text = "Icon Button",
                icon = Icons.Default.Add,
                onClick = {},
            )
        }
    }
}

