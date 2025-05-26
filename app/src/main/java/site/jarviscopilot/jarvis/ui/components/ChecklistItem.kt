package site.jarviscopilot.jarvis.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// Represents the different types of checklist items
enum class ChecklistItemType {
    NORMAL, WARNING, MANDATORY
}

// A customized checklist item that uses the Jarvis theme colors
@Composable
fun ChecklistItem(
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

// Parameter provider for JarvisChecklistItem previews
class ChecklistItemParameterProvider : PreviewParameterProvider<ChecklistItemType> {
    @Suppress("unused")
    override val values = sequenceOf(
        ChecklistItemType.NORMAL,
        ChecklistItemType.WARNING,
        ChecklistItemType.MANDATORY
    )
}

// Preview for JarvisChecklistItem in both light and dark themes
@PreviewLightDark
@Preview(
    name = "Jarvis Checklist Item",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisChecklistItemPreview(
    @PreviewParameter(ChecklistItemParameterProvider::class) type: ChecklistItemType
) {
    JarvisTheme {
        Column(Modifier.padding(8.dp)) {
            // Normal item
            ChecklistItem(
                text = "Uncompleted ${type.name.lowercase()} item",
                isCompleted = false,
                type = type,
                onItemClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Completed item
            ChecklistItem(
                text = "Completed ${type.name.lowercase()} item",
                isCompleted = true,
                type = type,
                onItemClick = {}
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Active item
            ChecklistItem(
                text = "Active ${type.name.lowercase()} item",
                isCompleted = false,
                type = type,
                isActive = true,
                onItemClick = {}
            )
        }
    }
}
