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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.model.ChecklistSection
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun SectionSelector(
    sections: List<ChecklistSection>,
    selectedIndex: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    val scrollState = rememberScrollState()

    // Auto-scroll to the selected index when it changes
    LaunchedEffect(selectedIndex, sections) {
        // Short delay to ensure the layout is ready
        delay(100)

        // Calculate approximate scroll position based on item width
        // Section items might be wider due to varying text lengths
        val itemWidth = 160 + 32 // 160dp average for text + 32dp for horizontal padding
        val targetScrollPosition = (selectedIndex * itemWidth).coerceAtMost(scrollState.maxValue)

        scrollState.animateScrollTo(targetScrollPosition)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .background(Color.Black)
            .padding(vertical = 8.dp)
    ) {
        sections.forEachIndexed { index, section ->
            val isSelected = index == selectedIndex
            val backgroundColor = when {
                isSelected -> aviationColors.avBlue
                section.type == "reference" -> aviationColors.avYellow
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
            type = "reference",
            name = "Reference Info",
            nameAudio = "",
            defaultView = "referenceView",
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
        SectionSelector(
            sections = demoSections,
            selectedIndex = 1,
            onSectionSelected = {}
        )
    }
}

