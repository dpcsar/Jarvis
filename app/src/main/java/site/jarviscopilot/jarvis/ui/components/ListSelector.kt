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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ListSelector(
    lists: List<ChecklistList>,
    selectedIndex: Int,
    onListSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    val scrollState = rememberScrollState()

    // Store positions of each item
    val itemPositions = remember { mutableStateMapOf<Int, Float>() }
    val itemWidths = remember { mutableStateMapOf<Int, Int>() }

    // Auto-scroll to the selected index when it changes
    LaunchedEffect(selectedIndex, lists, itemPositions, itemWidths) {
        // Short delay to ensure all positions are measured
        delay(100)

        // If we have the position for the selected item, scroll to it
        if (itemPositions.containsKey(selectedIndex)) {
            val position = itemPositions[selectedIndex] ?: 0f
            scrollState.animateScrollTo(position.toInt().coerceAtMost(scrollState.maxValue))
        }
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .background(Color.DarkGray)
            .padding(vertical = 6.dp)
    ) {
        lists.forEachIndexed { index, list ->
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) aviationColors.avGreen else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onListSelected(index) }
                    .onGloballyPositioned { coordinates ->
                        // Save the item's x position and width
                        itemPositions[index] = coordinates.positionInParent().x
                        itemWidths[index] = coordinates.size.width
                    }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (list.selectorName.isNotBlank()) list.selectorName else list.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else aviationColors.textOnSurface
                )
            }
        }
    }
}

@Preview(apiLevel = 35)
@Composable
fun ListSelectorPreview() {
    JarvisTheme {
        val demoLists = listOf(
            ChecklistList(
                name = "Pilot currency and proficiency",
                nameAudio = "",
                items = emptyList()
            ),
            ChecklistList(
                name = "Aircraft documents",
                nameAudio = "",
                items = emptyList()
            ),
            ChecklistList(
                name = "Weather and NOTAMs",
                nameAudio = "",
                items = emptyList()
            )
        )

        ListSelector(
            lists = demoLists,
            selectedIndex = 1,
            onListSelected = {}
        )
    }
}
