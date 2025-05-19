package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A component that displays a list of checklist items in a standard list view.
 *
 * @param list The list of checklist items to display
 * @param currentItemIndex The index of the currently selected item
 * @param onItemClick Callback for when an item is clicked
 * @param listState The state of the lazy list for controlling scroll position
 * @param sectionType The type of section (e.g., "emergency", "reference", or "checklist")
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun ListView(
    list: ChecklistList?,
    currentItemIndex: Int,
    onItemClick: (Int) -> Unit,
    listState: LazyListState,
    sectionType: String,
    modifier: Modifier = Modifier
) {
    if (list == null) {
        Text(
            text = "No items to display",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        // Show items in their original order
        items(list.items.size) { itemIndex ->
            val item = list.items[itemIndex]
            val isSelected = itemIndex == currentItemIndex

            Item(
                item = item,
                isSelected = isSelected,
                onClick = { onItemClick(itemIndex) },
                modifier = Modifier.padding(vertical = 4.dp),
                sectionType = sectionType // Pass section type
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemListViewPreview() {
    // Create sample data for preview
    val sampleList = ChecklistList(
        name = "Preflight Inspection",
        nameAudio = "",
        items = listOf(
            ChecklistItem(
                type = "item",
                label1 = "Preflight Inspection",
                label1Audio = "",
                label2 = "COMPLETE",
                label2Audio = "",
                mandatory = true
            ),
            ChecklistItem(
                type = "item",
                label1 = "Control Lock",
                label1Audio = "",
                label2 = "REMOVE",
                label2Audio = "",
                mandatory = true
            ),
            ChecklistItem(
                type = "item",
                label1 = "Seats & Belts",
                label1Audio = "",
                label2 = "ADJUST, SECURE",
                label2Audio = "",
                mandatory = true
            )
        )
    )

    JarvisTheme {
        ListView(
            list = sampleList,
            currentItemIndex = 1,  // Second item selected
            onItemClick = { },      // No-op for preview
            listState = rememberLazyListState(),
            sectionType = "checklist"
        )
    }
}

