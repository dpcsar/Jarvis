package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.ChecklistList
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A composable that displays a horizontal row of selectable list cards.
 * Used to allow users to switch between different lists within a section.
 *
 * @param lists The list of available checklist lists
 * @param selectedListIndex The index of the currently selected list
 * @param onListSelected Callback when a list is selected
 * @param isNormalListView Whether this is displayed in normal list view mode
 * @param completedItemsByList List of completed items for each list (indices match the lists parameter)
 * @param modifier Optional modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListSelector(
    modifier: Modifier = Modifier,
    lists: List<ChecklistList>,
    selectedListIndex: Int,
    onListSelected: (Int) -> Unit,
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
            .padding(vertical = 8.dp),
        color = JarvisTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lists.size) { index ->
                val list = lists[index]
                val isSelected = index == selectedListIndex

                Card(
                    onClick = { onListSelected(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            JarvisTheme.colorScheme.primaryContainer
                        else
                            JarvisTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
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

                        // Only show progress bar for normalListView mode
                        if (isNormalListView) {
                            // Calculate progress for this specific list
                            val totalItems = list.listItems.size

                            // Get the completed items for this specific list
                            val completedCount = if (index < completedItemsByList.size) {
                                completedItemsByList[index].size
                            } else {
                                0
                            }

                            val progress =
                                if (totalItems > 0) completedCount.toFloat() / totalItems else 0f

                            // Custom progress indicator implementation
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .height(4.dp)
                                    .width(80.dp)  // Use a fixed width that's smaller than typical card width
                                    .background(
                                        color = if (isSelected)
                                            JarvisTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                        else
                                            JarvisTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(80.dp * progress)  // Scale the progress bar width
                                        .background(
                                            color = if (isSelected)
                                                JarvisTheme.colorScheme.onPrimaryContainer
                                            else
                                                JarvisTheme.colorScheme.onSurfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
