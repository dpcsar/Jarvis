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
import site.jarviscopilot.jarvis.ui.theme.AvBlack
import site.jarviscopilot.jarvis.ui.theme.AvGreen
import site.jarviscopilot.jarvis.ui.theme.AvLightGrey
import site.jarviscopilot.jarvis.ui.theme.AvRed
import site.jarviscopilot.jarvis.ui.theme.AvWhite
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ChecklistItemComponent(
    item: ChecklistItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> AvLightGrey
        item.checked -> Color.White
        else -> Color.White
    }
    
    val borderColor = when {
        isSelected -> AvBlack
        item.mandatory -> AvRed
        else -> Color.Transparent
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
            .clickable(enabled = item.enabled) { onClick() }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (item.checked) AvGreen else AvWhite,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = AvBlack,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.checked) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = AvWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Item content
            Column(modifier = Modifier.weight(1f)) {
                if (item.label1.isNotBlank()) {
                    Text(
                        text = item.label1.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (item.label2.isNotBlank()) {
                    Text(
                        text = item.label2.trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (item.label3.isNotBlank()) {
                    Text(
                        text = item.label3.trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                if (item.comments.isNotBlank()) {
                    Text(
                        text = "Note: ${item.comments}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
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
        ChecklistItemComponent(
            item = ChecklistItem(
                id = "1",
                type = "item",
                checked = false,
                visible = true,
                enabled = true,
                label1 = "Pitot Heat",
                label2 = "Test",
                label3 = "",
                mandatory = true,
                comments = "Ensure indicator light is on"
            ),
            isSelected = true,
            onClick = {}
        )
    }
}