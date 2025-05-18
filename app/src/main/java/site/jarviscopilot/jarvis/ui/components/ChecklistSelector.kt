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
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

@Composable
fun ChecklistSelector(
    lists: List<ChecklistList>,
    selectedIndex: Int,
    onListSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val aviationColors = LocalAviationColors.current
    
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .background(Color.Black)
            .padding(vertical = 8.dp)
    ) {
        lists.forEachIndexed { index, list ->
            val isSelected = index == selectedIndex
            val backgroundColor = when {
                isSelected -> aviationColors.avBlue
                list.name.contains("Emergency", ignoreCase = true) -> aviationColors.avRed
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
                    .clickable { onListSelected(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = list.name,
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
    val demoLists = listOf(
        ChecklistList(
            id = "1",
            name = "PreFlight",
            type = "list",
            completionState = "n",
            visible = true,
            children = emptyList(),
            color = "black",
            defaultView = "checklistView"
        ),
        ChecklistList(
            id = "2",
            name = "Before Takeoff",
            type = "list",
            completionState = "n",
            visible = true,
            children = emptyList(),
            color = "black",
            defaultView = "checklistView"
        ),
        ChecklistList(
            id = "3",
            name = "InFlight",
            type = "list",
            completionState = "n",
            visible = true,
            children = emptyList(),
            color = "black",
            defaultView = "checklistView"
        ),
        ChecklistList(
            id = "4",
            name = "Emergency",
            type = "list",
            completionState = "n",
            visible = true,
            children = emptyList(),
            color = "red",
            defaultView = "checklistView"
        )
    )
    
    JarvisTheme {
        ChecklistSelector(
            lists = demoLists,
            selectedIndex = 1,
            onListSelected = {}
        )
    }
}