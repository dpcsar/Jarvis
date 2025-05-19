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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sectionType: String = "normal" // Add parameter for section type
) {
    val aviationColors = LocalAviationColors.current
    
    val backgroundColor = when {
        isSelected -> aviationColors.selectedItemBackground
        item.checked -> aviationColors.itemBackground
        else -> aviationColors.itemBackground
    }
    
    val borderColor = when {
        isSelected -> aviationColors.avBlack
        item.mandatory -> aviationColors.avRed
        else -> Color.Transparent
    }
    
    // Use different styling for non-standard item types
    val isNormalItem = item.type == "item" 
    val isWarningItem = item.type == "warning"
    val isCautionItem = item.type == "caution"
    val isNoteItem = item.type == "note"
    val isLabelItem = item.type == "label"
    
    // Determine if we should show check circles based on section type
    val showCheckCircle = isNormalItem && sectionType != "reference"

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
            .clickable { onClick() }
            .padding(8.dp)
    ) {
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
                        ),
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
            
            // Item content
            Column(modifier = Modifier.weight(1f)) {
                if (item.label1.isNotBlank()) {
                    Text(
                        text = item.label1.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = fontWeight,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (item.label2.isNotBlank()) {
                    Text(
                        text = item.label2.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isNormalItem) FontWeight.Bold else fontWeight,
                        color = textColor,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = if (isNormalItem) 8.dp else 0.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ChecklistItemComponentPreview() {
    JarvisTheme {
        Column {
            Item(
                item = ChecklistItem(
                    type = "item",
                    label1 = "Pitot Heat",
                    label1Audio = "",
                    label2 = "TEST",
                    label2Audio = "",
                    mandatory = true
                ),
                isSelected = true,
                onClick = {}
            )
            
            Item(
                item = ChecklistItem(
                    type = "warning",
                    label1 = "WARNING: Do not operate heater on the ground for more than 30 seconds",
                    label1Audio = "",
                    label2 = "",
                    label2Audio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {}
            )
            
            Item(
                item = ChecklistItem(
                    type = "note",
                    label1 = "Note: Wait for at least 2 minutes between cranking attempts",
                    label1Audio = "",
                    label2 = "",
                    label2Audio = "",
                    mandatory = false
                ),
                isSelected = false,
                onClick = {}
            )
        }
    }
}


