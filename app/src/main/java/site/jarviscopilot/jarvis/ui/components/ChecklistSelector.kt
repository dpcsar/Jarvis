package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.model.ChecklistSection
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ChecklistSelector(
    sections: List<ChecklistSection>,
    selectedIndex: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .background(Color.Black)
            .padding(vertical = 8.dp)
    ) {
        sections.forEachIndexed { index, section ->
            val isSelected = index == selectedIndex
            val backgroundColor = when {
                isSelected -> aviationColors.avBlue
                section.name.contains("Emergency", ignoreCase = true) -> aviationColors.avRed
                else -> Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSectionSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = section.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = aviationColors.textOnSurface
                )
            }
        }
    }
}

@Preview
@Composable
fun ChecklistSelectorPreview() {
    val demoSections = listOf(
        ChecklistSection(
            type = "checklist",
            name = "PreFlight",
            nameAudio = "",
            defaultView = "checklistView",
            lists = emptyList()
        ),
        ChecklistSection(
            type = "checklist",
            name = "Before Takeoff",
            nameAudio = "",
            defaultView = "checklistView",
            lists = emptyList()
        ),
        ChecklistSection(
            type = "checklist",
            name = "InFlight",
            nameAudio = "",
            defaultView = "checklistView",
            lists = emptyList()
        ),
        ChecklistSection(
            type = "emergency",
            name = "Emergency",
            nameAudio = "",
            defaultView = "onePageView",
            lists = emptyList()
        )
    )
    
    JarvisTheme {
        ChecklistSelector(
            sections = demoSections,
            selectedIndex = 1,
            onSectionSelected = {}
        )
    }
}