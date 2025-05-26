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
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// Represents the different types of checklist items
enum class ChecklistItemType {
    TASK, NOTE, LABEL, CAUTION, WARNING
}

// A customized checklist item that uses the Jarvis theme colors
@Composable
fun ChecklistItem(
    text: String,
    isCompleted: Boolean = false,
    type: ChecklistItemType = ChecklistItemType.TASK,
    isActive: Boolean = false,
    onItemClick: () -> Unit
) {
    // Only TASK items can be completed or active
    val effectiveIsCompleted = if (type == ChecklistItemType.TASK) isCompleted else false
    val effectiveIsActive = if (type == ChecklistItemType.TASK) isActive else false

    val backgroundColor by animateColorAsState(
        when {
            effectiveIsActive -> JarvisTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            effectiveIsCompleted -> JarvisTheme.colorScheme.surfaceVariant
            else -> JarvisTheme.colorScheme.surface
        }
    )

    val borderColor by animateColorAsState(
        when (type) {
            ChecklistItemType.WARNING -> JarvisTheme.colorScheme.emergency
            ChecklistItemType.CAUTION -> JarvisTheme.colorScheme.emergency.copy(alpha = 0.7f)
            ChecklistItemType.LABEL -> JarvisTheme.colorScheme.tertiary
            ChecklistItemType.NOTE -> JarvisTheme.colorScheme.secondary
            ChecklistItemType.TASK -> Color.Transparent
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (type != ChecklistItemType.TASK) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onItemClick() },
        color = backgroundColor,
        shadowElevation = if (effectiveIsActive) 4.dp else 1.dp
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
                // Icons for specific types
                when (type) {
                    ChecklistItemType.WARNING -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = JarvisTheme.colorScheme.emergency,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    ChecklistItemType.CAUTION -> {
                        Icon(
                            imageVector = Icons.Default.Warning, // Reuse warning icon but with different styling
                            contentDescription = "Caution",
                            tint = JarvisTheme.colorScheme.emergency.copy(alpha = 0.7f),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    else -> { /* No icon for other types */ }
                }

                Text(
                    text = text,
                    style = JarvisTheme.typography.bodyLarge,
                    color = JarvisTheme.colorScheme.onSurface,
                    textDecoration = if (effectiveIsCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = when (type) {
                        ChecklistItemType.LABEL -> FontWeight.Bold
                        ChecklistItemType.NOTE -> FontWeight.Medium
                        else -> if (effectiveIsActive) FontWeight.Medium else FontWeight.Normal
                    }
                )
            }

            // Completed checkmark
            if (effectiveIsCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = JarvisTheme.colorScheme.primary
                )
            }
        }
    }
}

// Preview for JarvisChecklistItem in both light and dark themes
@PreviewLightDark
@Preview(
    name = "Jarvis Checklist Item",
    showBackground = true,
    apiLevel = 35
)
@Composable
fun JarvisChecklistItemPreview() {
    JarvisTheme {
        Column(Modifier.padding(8.dp)) {
            // Show each type with appropriate styling
            Text(
                text = "Task items",
                style = JarvisTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Normal task
            ChecklistItem(
                text = "Regular task item",
                type = ChecklistItemType.TASK,
                onItemClick = {}
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Completed task
            ChecklistItem(
                text = "Completed task item",
                isCompleted = true,
                type = ChecklistItemType.TASK,
                onItemClick = {}
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Active task
            ChecklistItem(
                text = "Active task item",
                isActive = true,
                type = ChecklistItemType.TASK,
                onItemClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Note item
            Text(
                text = "Note item",
                style = JarvisTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ChecklistItem(
                text = "This is a note item",
                type = ChecklistItemType.NOTE,
                onItemClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Label item
            Text(
                text = "Label item",
                style = JarvisTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ChecklistItem(
                text = "This is a label item",
                type = ChecklistItemType.LABEL,
                onItemClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Caution item
            Text(
                text = "Caution item",
                style = JarvisTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ChecklistItem(
                text = "This is a caution item",
                type = ChecklistItemType.CAUTION,
                onItemClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Warning item
            Text(
                text = "Warning item",
                style = JarvisTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ChecklistItem(
                text = "This is a warning item",
                type = ChecklistItemType.WARNING,
                onItemClick = {}
            )
        }
    }
}
