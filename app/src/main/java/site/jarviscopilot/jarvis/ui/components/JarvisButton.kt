package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            containerColor = JarvisTheme.colorScheme.primary,
            contentColor = JarvisTheme.colorScheme.onPrimary,
            disabledContainerColor = JarvisTheme.colorScheme.surfaceVariant,
            disabledContentColor = JarvisTheme.colorScheme.onSurfaceVariant
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
            contentColor = JarvisTheme.colorScheme.primary,
            disabledContentColor = JarvisTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = contentPadding,
        content = content
    )
}

// A button with both text and icon that uses the Jarvis theme
@Composable
fun JarvisIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconTint: Color = JarvisTheme.colorScheme.onPrimary,
    containerColor: Color = JarvisTheme.colorScheme.primary,
    text: String? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = JarvisTheme.colorScheme.onPrimary,
            disabledContainerColor = JarvisTheme.colorScheme.surfaceVariant,
            disabledContentColor = JarvisTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = if (text != null) 16.dp else 8.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )

        // Only show text if provided
        if (text != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = JarvisTheme.typography.bodyMedium,
                color = JarvisTheme.colorScheme.onPrimary
            )
        }
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
                icon = Icons.Default.Add,
                onClick = {},
            )
        }
    }
}

