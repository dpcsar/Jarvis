package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors

@Composable
fun Item(
    item: ChecklistItem,
    isSelected: Boolean,
    onClick: () -> Unit, // This will be used only for the check circle
    modifier: Modifier = Modifier,
    sectionType: String = "normal",
    onCheckCircleClick: () -> Unit = onClick, // For toggling the check
    onTextClick: () -> Unit = {} // New parameter for text click that just focuses without toggling
) {
    val aviationColors = LocalAviationColors.current

    val backgroundColor = when {
        isSelected -> aviationColors.selectedItemBackground
        item.checked -> aviationColors.itemBackground
        else -> aviationColors.itemBackground
    }
    
    // Use different styling for non-standard item types
    val isNormalItem = item.type == "item" 
    val isWarningItem = item.type == "warning"
    val isCautionItem = item.type == "caution"
    val isNoteItem = item.type == "note"
    val isLabelItem = item.type == "label"
    
    // Special types that need different layout
    val isSpecialType = isWarningItem || isCautionItem || isNoteItem || isLabelItem

    // Determine if we should show check circles based on section type
    val showCheckCircle = isNormalItem && sectionType != "reference"

    val borderColor = when {
        isSelected -> aviationColors.avBlack
        // Only show mandatory styling for normal items, not for special types (labels, cautions, warnings, notes)
        item.mandatory && isNormalItem -> aviationColors.avRed
        else -> Color.Transparent
    }

    val textColor = when {
        isWarningItem -> aviationColors.avRed
        isCautionItem -> aviationColors.avAmber
        isNoteItem -> aviationColors.avBlue
        else -> aviationColors.textOnSurface
    }
    
    val fontWeight = when {
        isLabelItem || isWarningItem || isCautionItem -> FontWeight.Bold
        else -> FontWeight.Normal
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected || item.mandatory) 2.dp else 0.dp, 
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = sectionType != "reference") { onTextClick() }  // Text click only focuses item without toggling check
            .padding(8.dp)
    ) {
        if (isSpecialType) {
            // For warning, caution, and note items - use vertical layout with response below challenge
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item.challenge.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = fontWeight,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.response.isNotBlank()) {
                    Spacer(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = item.response.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            // Normal item layout with challenge and response side by side
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator - only show for normal items in non-reference sections
                if (showCheckCircle) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (item.checked) aviationColors.avGreen else aviationColors.avWhite,
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = aviationColors.avBlack,
                                shape = CircleShape
                            )
                            .clickable { onCheckCircleClick() }, // Use the dedicated check circle handler
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.checked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = aviationColors.avWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Challenge text on the left (max 48% width)
                if (item.challenge.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(0.48f)
                            .wrapContentWidth(Alignment.Start)
                    ) {
                        Text(
                            text = item.challenge.trim(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = fontWeight,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Flexible spacer in the middle
                Spacer(modifier = Modifier.weight(0.04f))

                // Response text on the right (max 48% width)
                if (item.response.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(0.48f)
                            .wrapContentWidth(Alignment.End)
                    ) {
                        Text(
                            text = item.response.trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isNormalItem) FontWeight.Bold else fontWeight,
                            color = textColor,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(apiLevel = 35)
@Composable
fun ChecklistItemComponentPreview() {
    JarvisTheme {
        Column {
            Item(
                item = ChecklistItem(
                    type = "item",
                    challenge = "Pitot Heat",
                    challengeAudio = "",
                    response = "TEST",
                    responseAudio = "",
                    mandatory = true
                ),
                isSelected = true,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )
            

            Item(
                item = ChecklistItem(
                    type = "note",
                    challenge = "Wait for at least 2 minutes between cranking attempts",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )

            Item(
                item = ChecklistItem(
                    type = "warning",
                    challenge = "Do not operate heater on the ground for more than 30 seconds",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )

            Item(
                item = ChecklistItem(
                    type = "caution",
                    challenge = "Do not operate heater on the ground for more than 30 seconds",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )
        }
    }
}

@Preview(apiLevel = 35)
@Composable
fun ChecklistItemComponentPreviewDark() {
    JarvisTheme(darkTheme = true) {
        Column {
            Item(
                item = ChecklistItem(
                    type = "item",
                    challenge = "Pitot Heat",
                    challengeAudio = "",
                    response = "TEST",
                    responseAudio = "",
                    mandatory = true
                ),
                isSelected = true,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )


            Item(
                item = ChecklistItem(
                    type = "note",
                    challenge = "Wait for at least 2 minutes between cranking attempts",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )

            Item(
                item = ChecklistItem(
                    type = "warning",
                    challenge = "Do not operate heater on the ground for more than 30 seconds",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )

            Item(
                item = ChecklistItem(
                    type = "caution",
                    challenge = "Do not operate heater on the ground for more than 30 seconds",
                    challengeAudio = "",
                    response = "second line",
                    responseAudio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {},
                onCheckCircleClick = {}, // Add the parameter to preview
                onTextClick = {} // Add the parameter to preview
            )
        }
    }
}
