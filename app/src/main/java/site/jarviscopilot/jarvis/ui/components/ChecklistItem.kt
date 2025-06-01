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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// Represents the different types of checklist items
enum class ChecklistItemType {
    TASK, NOTE, LABEL, CAUTION, WARNING, REFERENCE, REFERENCENOTE
}

// A customized checklist item that uses the Jarvis theme colors
@Composable
fun ChecklistItem(
    challenge: String,
    response: String = "",
    isCompleted: Boolean = false,
    type: ChecklistItemType = ChecklistItemType.TASK,
    isActive: Boolean = false,
    isBlocked: Boolean = false,
    onItemClick: () -> Unit,
    onCheckboxClick: () -> Unit
) {
    // Only TASK items can be completed or active
    val effectiveIsCompleted = if (type == ChecklistItemType.TASK) isCompleted else false
    val effectiveIsActive = if (type == ChecklistItemType.TASK) isActive else false
    val effectiveIsBlocked = if (type == ChecklistItemType.TASK) isBlocked else false

    // Don't show response for LABEL items
    val displayResponse = if (type == ChecklistItemType.LABEL) "" else response

    val backgroundColor by animateColorAsState(
        when {
            effectiveIsActive -> JarvisTheme.colorScheme.primaryContainer
            effectiveIsCompleted -> JarvisTheme.colorScheme.surfaceVariant
            else -> JarvisTheme.colorScheme.surface
        }
    )

    val borderColor by animateColorAsState(
        when (type) {
            ChecklistItemType.WARNING -> JarvisTheme.colorScheme.warning
            ChecklistItemType.CAUTION -> JarvisTheme.colorScheme.caution
            ChecklistItemType.LABEL -> JarvisTheme.colorScheme.tertiary
            ChecklistItemType.NOTE -> JarvisTheme.colorScheme.secondary
            ChecklistItemType.REFERENCE -> JarvisTheme.colorScheme.reference
            ChecklistItemType.REFERENCENOTE -> JarvisTheme.colorScheme.reference
            ChecklistItemType.TASK -> Color.Transparent
        }
    )

    // Apply different text color for blocked tasks
    val textColor by animateColorAsState(
        when {
            effectiveIsBlocked -> JarvisTheme.colorScheme.onSurface.copy(alpha = 0.5f) // Dim text for blocked tasks
            else -> JarvisTheme.colorScheme.onSurface
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
            // Make note, caution, warning and reference items clickable at the container level for TTS
            .let {
                if (type != ChecklistItemType.TASK && type != ChecklistItemType.LABEL) {
                    it.clickable { onItemClick() }
                } else {
                    it
                }
            },
        color = backgroundColor,
        shadowElevation = if (effectiveIsActive) 4.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (type == ChecklistItemType.TASK) {
                // Task layout with challenge on left and response on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Checkbox (only the checkbox triggers completion)
                    Checkbox(
                        checked = effectiveIsCompleted,
                        onCheckedChange = { onCheckboxClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = JarvisTheme.colorScheme.primary,
                            uncheckedColor = JarvisTheme.colorScheme.outline
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // All text content is in a single clickable row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onItemClick() }
                    ) {
                        // Challenge text on the left
                        Text(
                            text = challenge,
                            style = JarvisTheme.typography.bodyLarge,
                            color = textColor,
                            textDecoration = if (effectiveIsCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            fontWeight = if (effectiveIsActive) FontWeight.Medium else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        // Response on the right - only if not empty
                        if (response.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = response,
                                style = JarvisTheme.typography.bodyLarge,
                                color = textColor, // Same color as challenge text
                                fontWeight = if (effectiveIsActive) FontWeight.Medium else FontWeight.Normal,
                                textAlign = TextAlign.End,
                                textDecoration = if (effectiveIsCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            } else if (type == ChecklistItemType.REFERENCE) {
                // Reference layout similar to task but without checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Challenge text on the left
                    Text(
                        text = challenge,
                        style = JarvisTheme.typography.bodyLarge,
                        color = textColor,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    // Response on the right - only if not empty
                    if (response.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = response,
                            style = JarvisTheme.typography.bodyLarge,
                            color = textColor,
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                // Non-task layout (updated for challenge-response format)
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icons for specific types
                        when (type) {
                            ChecklistItemType.WARNING -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = JarvisTheme.colorScheme.emergency, // Red color
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            ChecklistItemType.CAUTION -> {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Caution",
                                    tint = JarvisTheme.colorScheme.caution,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            else -> { /* No icon for other types */
                            }
                        }

                        Text(
                            text = challenge,
                            style = JarvisTheme.typography.bodyLarge,
                            color = JarvisTheme.colorScheme.onSurface,
                            fontWeight = when (type) {
                                ChecklistItemType.LABEL -> FontWeight.Bold
                                ChecklistItemType.NOTE -> FontWeight.Medium
                                ChecklistItemType.REFERENCENOTE -> FontWeight.Medium
                                else -> FontWeight.Normal
                            }
                        )
                    }

                    // Add divider line and response text if it's not empty and not a LABEL type
                    if (displayResponse.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp),
                            color = JarvisTheme.colorScheme.outlineVariant
                        ) {}
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = displayResponse,
                            style = JarvisTheme.typography.bodyMedium,
                            color = JarvisTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(start = if (type == ChecklistItemType.WARNING || type == ChecklistItemType.CAUTION) 32.dp else 0.dp)
                        )
                    }
                }
            }
        }
    }
}
