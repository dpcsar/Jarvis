package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import site.jarviscopilot.jarvis.data.model.ChecklistSectionData
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A composable that displays a horizontal row of selectable section cards.
 * Used to allow users to switch between different sections of a checklist.
 */
@Composable
fun SectionSelector(
    sections: List<ChecklistSectionData>,
    selectedSectionIndex: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    completedItemsBySection: List<List<MutableList<Int>>> = emptyList()
) {
    // Calculate the total number of items in a section by summing up all items in all lists
    fun calculateTotalItemsForSection(section: ChecklistSectionData): Int {
        return section.lists.sumOf { it.listItems.size }
    }

    // Remember scroll state to control scrolling behavior
    val listState = rememberLazyListState()

    // Find the index of the last checklist type section
    val lastChecklistSectionIndex = sections.indexOfLast { it.sectionType == "checklist" }

    // When selected section changes, scroll to center it
    LaunchedEffect(selectedSectionIndex) {
        if (sections.isNotEmpty()) {
            // For the last checklist section, ensure it's visible before centering
            if (selectedSectionIndex == lastChecklistSectionIndex) {
                // First, directly scroll to ensure the section is visible
                listState.scrollToItem(selectedSectionIndex)

                // Wait for layout to complete - delay is the most reliable approach here
                kotlinx.coroutines.delay(200)

                // After layout is guaranteed complete, do a direct centering calculation
                val item =
                    listState.layoutInfo.visibleItemsInfo.find { it.index == selectedSectionIndex }
                if (item != null) {
                    val viewportCenter =
                        (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2
                    val itemCenter = item.offset + (item.size / 2)
                    val scrollBy = itemCenter - viewportCenter
                    listState.animateScrollBy(scrollBy.toFloat())
                    return@LaunchedEffect  // Skip the rest of the code for this section
                }
            }

            // Standard centering for other sections
            // Get the item's layout info to calculate the centering offset
            listState.layoutInfo.visibleItemsInfo.find { it.index == selectedSectionIndex }
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
                listState.animateScrollToItem(selectedSectionIndex)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),  // Reduced from 8.dp to 4.dp
        color = JarvisTheme.colorScheme.surface,
        contentColor = JarvisTheme.colorScheme.onSurface,
        tonalElevation = 4.dp
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.padding(6.dp),  // Reduced from 8.dp to 6.dp
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections.size) { index ->
                val section = sections[index]
                val isSelected = index == selectedSectionIndex

                // Define background color based on section type and selection state
                val backgroundColor = when {
                    section.sectionType == "emergency" && isSelected ->
                        JarvisTheme.colorScheme.emergencyContainer

                    section.sectionType == "emergency" && !isSelected ->
                        JarvisTheme.colorScheme.emergency.copy(alpha = 0.7f)

                    section.sectionType == "reference" && isSelected ->
                        JarvisTheme.colorScheme.referenceContainer

                    section.sectionType == "reference" && !isSelected ->
                        JarvisTheme.colorScheme.reference.copy(alpha = 0.7f)

                    section.sectionType == "checklist" ->
                        if (isSelected)
                            JarvisTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        else
                            JarvisTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)

                    isSelected ->
                        JarvisTheme.colorScheme.primaryContainer

                    else ->
                        JarvisTheme.colorScheme.surfaceVariant
                }

                // Define text color based on background for proper contrast
                val textColor = when {
                    section.sectionType == "emergency" && isSelected ->
                        JarvisTheme.colorScheme.onEmergencyContainer

                    section.sectionType == "emergency" && !isSelected ->
                        JarvisTheme.colorScheme.onEmergency

                    section.sectionType == "reference" && isSelected ->
                        JarvisTheme.colorScheme.onReferenceContainer

                    section.sectionType == "reference" && !isSelected ->
                        JarvisTheme.colorScheme.onReference

                    section.sectionType == "checklist" ->
                        if (isSelected)
                            JarvisTheme.colorScheme.onPrimaryContainer
                        else
                            JarvisTheme.colorScheme.onSurfaceVariant

                    isSelected ->
                        JarvisTheme.colorScheme.onPrimaryContainer

                    else ->
                        JarvisTheme.colorScheme.onSurfaceVariant
                }

                // Replace Card with Box to have full control over layout and gestures
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor)
                        .clickable { onSectionSelected(index) }
                ) {
                    if (section.sectionType == "checklist") {
                        // Calculate progress for checklist sections
                        val totalItems = calculateTotalItemsForSection(section)
                        val completedCount = if (index < completedItemsBySection.size) {
                            completedItemsBySection[index].sumOf { it.size }
                        } else {
                            0
                        }
                        val progress =
                            if (totalItems > 0) completedCount.toFloat() / totalItems else 0f

                        // Content column that will be on top of the progress indicator
                        val content = @Composable {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    )
                            ) {
                                Text(
                                    text = if (section.sectionSelectorName.isNotEmpty())
                                        section.sectionSelectorName
                                    else
                                        section.sectionTitle,
                                    style = JarvisTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Create progress overlay that fills from left to right
                        if (progress > 0) {
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
                    } else {
                        // For non-checklist sections, just display the content normally
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = if (section.sectionSelectorName.isNotEmpty())
                                    section.sectionSelectorName
                                else
                                    section.sectionTitle,
                                style = JarvisTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = textColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
