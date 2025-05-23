package site.jarviscopilot.jarvis.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

/**
 * Represents the different types of checklist items
 */
enum class ChecklistItemType {
    NORMAL, WARNING, MANDATORY
}

/**
 * A customized checklist item that uses the Jarvis theme colors
 */
@Composable
fun JarvisChecklistItem(
    text: String,
    isCompleted: Boolean,
    type: ChecklistItemType = ChecklistItemType.NORMAL,
    isActive: Boolean = false,
    onItemClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        when {
            isActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            isCompleted -> MaterialTheme.colorScheme.surfaceVariant
            else -> MaterialTheme.colorScheme.surface
        }
    )

    val borderColor by animateColorAsState(
        when (type) {
            ChecklistItemType.WARNING -> MaterialTheme.colorScheme.error
            ChecklistItemType.MANDATORY -> MaterialTheme.colorScheme.tertiary
            else -> Color.Transparent
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (type != ChecklistItemType.NORMAL) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onItemClick() },
        color = backgroundColor,
        shadowElevation = if (isActive) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Warning icon for warning type
                if (type == ChecklistItemType.WARNING) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = when {
                        type == ChecklistItemType.MANDATORY -> FontWeight.Bold
                        isActive -> FontWeight.Medium
                        else -> FontWeight.Normal
                    }
                )
            }

            // Completed checkmark
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
