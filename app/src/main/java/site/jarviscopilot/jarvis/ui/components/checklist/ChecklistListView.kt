package site.jarviscopilot.jarvis.ui.components.checklist

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import site.jarviscopilot.jarvis.data.model.ChecklistItemData
import site.jarviscopilot.jarvis.ui.components.ChecklistItem
import site.jarviscopilot.jarvis.util.ChecklistUtils

/**
 * A composable that displays a list of checklist items.
 */
@Composable
fun ChecklistListView(
    checklistItemData: List<ChecklistItemData>,
    completedItems: List<Int>,
    activeItemIndex: Int,
    onItemClick: (Int) -> Unit,
    onToggleComplete: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // This effect ensures we scroll to the active item on initial composition
    // or when resuming a saved checklist
    LaunchedEffect(Unit) {
        if (checklistItemData.isNotEmpty() && activeItemIndex < checklistItemData.size) {
            listState.scrollToItem(activeItemIndex)
        }
    }

    // Auto-scroll to active item when it changes
    LaunchedEffect(activeItemIndex) {
        if (checklistItemData.isNotEmpty()) {
            // First make sure the item is visible
            if (activeItemIndex < checklistItemData.size &&
                !listState.layoutInfo.visibleItemsInfo.any { it.index == activeItemIndex }
            ) {
                listState.animateScrollToItem(activeItemIndex)
                // Need to wait for the scroll to complete and layout to update
                delay(100)
            }

            // Get the item's layout info
            listState.layoutInfo.visibleItemsInfo.find { it.index == activeItemIndex }
                ?.let { itemInfo ->
                    // Calculate the center position of the viewport
                    val viewportCenter =
                        (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2

                    // Calculate how much to scroll so the item's center aligns with viewport center
                    val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                    val scrollBy = itemCenter - viewportCenter

                    // Scroll by the calculated amount with animation
                    listState.animateScrollBy(scrollBy.toFloat())
                } ?: run {
                // Fallback to just scrolling to the item
                listState.animateScrollToItem(activeItemIndex)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(checklistItemData) { index, item ->
            ChecklistItem(
                challenge = item.challenge,
                response = item.response,
                isCompleted = index in completedItems,
                type = ChecklistUtils.convertToItemType(item.listItemType),
                isActive = index == activeItemIndex,
                onItemClick = { onItemClick(index) },
                onCheckboxClick = { onToggleComplete(index) }
            )
        }
    }
}


