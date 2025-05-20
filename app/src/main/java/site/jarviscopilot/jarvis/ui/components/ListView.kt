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
 * @param onItemSelect Callback for when an item should only be selected without toggling check state
 * @param listState The state of the lazy list for controlling scroll position
 * @param sectionType The type of section (e.g., "emergency", "reference", or "checklist")
 * @param modifier Modifier for customizing the layout
 * @param onCheckCircleClick Callback for when the check circle is clicked
 */
@Composable
fun ListView(
    list: ChecklistList?,
    currentItemIndex: Int,
    onItemSelect: (Int) -> Unit,
    listState: LazyListState,
    sectionType: String,
    modifier: Modifier = Modifier,
    onCheckCircleClick: () -> Unit = {}, // For check circle clicks
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
                onClick = { /* No longer used directly */ },
                modifier = Modifier.padding(vertical = 4.dp),
                sectionType = sectionType, // Pass section type
                onCheckCircleClick = {
                    // First make sure this item is selected (if it's not already)
                    if (currentItemIndex != itemIndex) {
                        onItemSelect(itemIndex) // Just select without toggling
                    }
                    // Then use the check button functionality directly
                    onCheckCircleClick()
                },
                onTextClick = {
                    // Just select the item without toggling the check
                    if (currentItemIndex != itemIndex) {
                        onItemSelect(itemIndex) // Use the new parameter that only selects
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, apiLevel = 35)
@Composable
fun ItemListViewPreview() {
    // Create sample data for preview
    val sampleList = ChecklistList(
        name = "Preflight Inspection",
        nameAudio = "",
        items = listOf(
            ChecklistItem(
                type = "item",
                challenge = "Preflight Inspection",
                challengeAudio = "",
                response = "COMPLETE",
                responseAudio = "",
                mandatory = true
            ),
            ChecklistItem(
                type = "item",
                challenge = "Control Lock",
                challengeAudio = "",
                response = "REMOVE",
                responseAudio = "",
                mandatory = true
            ),
            ChecklistItem(
                type = "item",
                challenge = "Seats & Belts",
                challengeAudio = "",
                response = "ADJUST, SECURE",
                responseAudio = "",
                mandatory = true
            )
        )
    )

    JarvisTheme {
        ListView(
            list = sampleList,
            currentItemIndex = 1,  // Second item selected
            onItemSelect = { },    // No-op for preview
            listState = rememberLazyListState(),
            sectionType = "checklist"
        )
    }
}
