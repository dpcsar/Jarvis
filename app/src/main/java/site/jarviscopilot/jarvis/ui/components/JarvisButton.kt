package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    text: String? = null,
    onLongClick: (() -> Unit)? = null
) {
    // Using Box with combinedClickable instead of Button for proper long-click support
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) containerColor else JarvisTheme.colorScheme.surfaceVariant
            )
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(
                horizontal = if (text != null) 16.dp else 8.dp,
                vertical = 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else JarvisTheme.colorScheme.onSurfaceVariant
            )

            // Only show text if provided
            if (text != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = JarvisTheme.typography.bodyMedium,
                    color = if (enabled)
                        JarvisTheme.colorScheme.onPrimary
                    else
                        JarvisTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
