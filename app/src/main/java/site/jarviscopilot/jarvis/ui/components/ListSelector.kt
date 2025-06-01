package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.model.ChecklistListData
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A composable that displays a horizontal row of selectable list cards.
 * Used to allow users to switch between different lists within a section.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSelector(
    modifier: Modifier = Modifier,
    lists: List<ChecklistListData>,
    selectedListIndex: Int,
    onListSelected: (Int) -> Unit,
    onLongClick: () -> Unit = {}, // Added onLongClick parameter with default empty implementation
    isNormalListView: Boolean = false,
    completedItemsByList: List<List<Int>> = emptyList()
) {
    // Remember scroll state to control scrolling behavior
    val listState = rememberLazyListState()

    // When selected list changes, scroll to center it
    LaunchedEffect(selectedListIndex) {
        if (lists.isNotEmpty()) {
            // We need to scroll to position the item in the center
            // Get the item's layout info to calculate the centering offset
            listState.layoutInfo.visibleItemsInfo.find { it.index == selectedListIndex }
                ?.let { itemInfo ->
                    // Calculate the center position of the viewport
                    val viewportCenter =
                        (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2

                    // Calculate how much to scroll so the item's center aligns with viewport center
                    // Half the item width is used to target the center of the item, not its leading edge
                    val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                    val scrollBy = itemCenter - viewportCenter

                    // Scroll by the calculated amount
                    listState.animateScrollBy(scrollBy.toFloat())
                } ?: run {
                // Fallback if we can't find the item yet: just scroll to the item
                // This will position the leading edge but will be corrected once layout info is available
                listState.animateScrollToItem(selectedListIndex)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),  // Reduced from 8.dp to 4.dp
        color = JarvisTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.padding(6.dp),  // Reduced from 8.dp to 6.dp
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lists.size) { index ->
                val list = lists[index]
                val isSelected = index == selectedListIndex

                // Replace Card with Box to have full control over touch gestures
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected)
                                JarvisTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            else
                                JarvisTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { onListSelected(index) },
                                onLongPress = {
                                    // Select the list first if it's not already selected
                                    if (!isSelected) {
                                        onListSelected(index)
                                    }
                                    // Then trigger the long click action
                                    onLongClick()
                                }
                            )
                        }
                ) {
                    // Calculate progress for this specific list when in normalListView mode
                    val progress = if (isNormalListView) {
                        val totalItems = list.listItems.size
                        val completedCount = if (index < completedItemsByList.size) {
                            completedItemsByList[index].size
                        } else {
                            0
                        }
                        if (totalItems > 0) completedCount.toFloat() / totalItems else 0f
                    } else {
                        0f
                    }

                    // Content column that will be on top of the progress indicator
                    val content = @Composable {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (list.listSelectorName.isNotEmpty())
                                    list.listSelectorName
                                else
                                    list.listTitle,
                                style = JarvisTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected)
                                    JarvisTheme.colorScheme.onPrimaryContainer
                                else
                                    JarvisTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Create progress overlay that fills from left to right
                    if (isNormalListView && progress > 0) {
                        // First, place a Box with the desired size that will contain the progress indicator
                        Box(
                            modifier = Modifier.matchParentSize() // This ensures it matches the parent box size
                        ) {
                            // Then place the progress indicator inside this box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress) // This is the key - fill width based on progress
                                    .fillMaxHeight() // Now this will only fill the height of the containing box
                                    .background(
                                        if (isSelected)
                                            JarvisTheme.colorScheme.primaryContainer
                                        else
                                            JarvisTheme.colorScheme.surfaceVariant
                                    )
                            )
                        }
                    }

                    // Now draw the content on top
                    content()
                }
            }
        }
    }
}
