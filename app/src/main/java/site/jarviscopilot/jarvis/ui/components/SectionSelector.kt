package site.jarviscopilot.jarvis.ui.components

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.ChecklistSection
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

/**
 * A composable that displays a horizontal row of selectable section cards.
 * Used to allow users to switch between different sections of a checklist.
 *
 * @param sections The list of available checklist sections
 * @param selectedSectionIndex The index of the currently selected section
 * @param onSectionSelected Callback when a section is selected
 * @param modifier Optional modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSelector(
    sections: List<ChecklistSection>,
    selectedSectionIndex: Int,
    onSectionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remember scroll state to control scrolling behavior
    val listState = rememberLazyListState()

    // When selected section changes, scroll to center it
    LaunchedEffect(selectedSectionIndex) {
        if (sections.isNotEmpty()) {
            // We need to scroll to position the item in the center
            // Get the item's layout info to calculate the centering offset
            listState.layoutInfo.visibleItemsInfo.find { it.index == selectedSectionIndex }?.let { itemInfo ->
                // Calculate the center position of the viewport
                val viewportCenter = (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2

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
            .padding(vertical = 8.dp),
        color = JarvisTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections.size) { index ->
                val section = sections[index]
                val isSelected = index == selectedSectionIndex

                Card(
                    onClick = { onSectionSelected(index) },
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            section.sectionType == "emergency" && isSelected ->
                                JarvisTheme.colorScheme.emergencyContainer
                            section.sectionType == "emergency" && !isSelected ->
                                JarvisTheme.colorScheme.emergency.copy(alpha = 0.7f)
                            isSelected ->
                                JarvisTheme.colorScheme.primaryContainer
                            else ->
                                JarvisTheme.colorScheme.surfaceVariant
                        }
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = if (section.sectionSelectorName.isNotEmpty())
                                section.sectionSelectorName
                              else
                                section.sectionTitle,
                        style = JarvisTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                            section.sectionType == "emergency" && isSelected ->
                                JarvisTheme.colorScheme.onEmergencyContainer
                            section.sectionType == "emergency" && !isSelected ->
                                JarvisTheme.colorScheme.onEmergency
                            isSelected ->
                                JarvisTheme.colorScheme.onPrimaryContainer
                            else ->
                                JarvisTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
