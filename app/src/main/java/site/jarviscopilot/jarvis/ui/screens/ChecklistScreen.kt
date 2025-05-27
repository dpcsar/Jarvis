package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.ChecklistItem
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.data.ChecklistState
import site.jarviscopilot.jarvis.data.ChecklistStateManager
import site.jarviscopilot.jarvis.ui.components.ChecklistBar
import site.jarviscopilot.jarvis.ui.components.ChecklistItem
import site.jarviscopilot.jarvis.ui.components.ChecklistItemType
import site.jarviscopilot.jarvis.ui.components.ChecklistTile
import site.jarviscopilot.jarvis.ui.components.ClickableListTitle
import site.jarviscopilot.jarvis.ui.components.JarvisIconButton
import site.jarviscopilot.jarvis.ui.components.ListSelector
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

// Converts a string listItemType to a ChecklistItemType enum
private fun convertToItemType(listItemType: String): ChecklistItemType {
    return when (listItemType.lowercase()) {
        "task" -> ChecklistItemType.TASK
        "note" -> ChecklistItemType.NOTE
        "label" -> ChecklistItemType.LABEL
        "caution" -> ChecklistItemType.CAUTION
        "warning" -> ChecklistItemType.WARNING
        else -> ChecklistItemType.TASK // Default to TASK for any unknown types
    }
}

@Composable
private fun ChecklistListView(
    checklistItems: List<ChecklistItem>,
    completedItems: List<Int>,
    activeItemIndex: Int,
    onItemClick: (Int) -> Unit,
    onToggleComplete: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    // This effect ensures we scroll to the active item on initial composition
    // or when resuming a saved checklist
    LaunchedEffect(Unit) {
        if (checklistItems.isNotEmpty() && activeItemIndex < checklistItems.size) {
            listState.scrollToItem(activeItemIndex)
        }
    }

    // Auto-scroll to active item when it changes
    LaunchedEffect(activeItemIndex) {
        if (checklistItems.isNotEmpty()) {
            // First make sure the item is visible
            if (activeItemIndex < checklistItems.size &&
                !listState.layoutInfo.visibleItemsInfo.any { it.index == activeItemIndex }
            ) {
                listState.animateScrollToItem(activeItemIndex)
                // Need to wait for the scroll to complete and layout to update
                kotlinx.coroutines.delay(100)
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
        itemsIndexed(checklistItems) { index, item ->
            ChecklistItem(
                challenge = item.challenge,
                response = item.response,
                isCompleted = index in completedItems,
                type = convertToItemType(item.listItemType),
                isActive = index == activeItemIndex,
                onItemClick = { onItemClick(index) },
                onCheckboxClick = { onToggleComplete(index) }
            )
        }
    }
}

@Composable
fun ChecklistScreen(
    checklistName: String,
    onNavigateHome: () -> Unit,
    resumeFromSaved: Boolean = false
) {
    val context = LocalContext.current

    // Create a ChecklistRepository instance
    val repository = remember { ChecklistRepository(context) }

    // Create a ChecklistStateManager instance
    val stateManager = remember { ChecklistStateManager(context) }

    // Load checklist data from JSON file based on the checklist name
    val checklistData = remember(checklistName, repository) {
        repository.loadChecklist(checklistName)
    }

    // Track the currently selected section
    val selectedSectionIndex = remember { mutableIntStateOf(0) }

    // Track the currently selected list within a section
    val selectedListIndex = remember { mutableIntStateOf(0) }

    // Get current view mode from section (normalListView or tileListView)
    val currentViewMode = remember(checklistData, selectedSectionIndex.intValue) {
        if (checklistData != null &&
            selectedSectionIndex.intValue < checklistData.sections.size
        ) {
            checklistData.sections[selectedSectionIndex.intValue].listView
        } else {
            "normalListView" // Default to normal list view
        }
    }

    // Get lists from the selected section
    val currentSectionLists = remember(checklistData, selectedSectionIndex.intValue) {
        if (checklistData != null &&
            selectedSectionIndex.intValue < checklistData.sections.size
        ) {
            checklistData.sections[selectedSectionIndex.intValue].lists
        } else {
            emptyList()
        }
    }

    // Extract checklist items from the loaded data based on selected section and list
    val checklistItems =
        remember(checklistData, selectedSectionIndex.intValue, selectedListIndex.intValue) {
            if (checklistData != null &&
                selectedSectionIndex.intValue < checklistData.sections.size &&
                selectedListIndex.intValue < checklistData.sections[selectedSectionIndex.intValue].lists.size
            ) {
                checklistData.sections[selectedSectionIndex.intValue].lists[selectedListIndex.intValue].listItems
            } else {
                emptyList()
            }
        }

    // Track which items are completed (per section and list)
    val completedItemsBySection = remember(checklistData) {
        checklistData?.sections?.map { section ->
            section.lists.map { mutableStateListOf<Int>() }
        }
            ?: listOf(listOf(mutableStateListOf<Int>()))
    }

    // Current section's and list's completed items
    val completedItems = remember(
        selectedSectionIndex.intValue,
        selectedListIndex.intValue,
        completedItemsBySection
    ) {
        if (selectedSectionIndex.intValue < completedItemsBySection.size &&
            selectedListIndex.intValue < completedItemsBySection[selectedSectionIndex.intValue].size
        ) {
            completedItemsBySection[selectedSectionIndex.intValue][selectedListIndex.intValue]
        } else {
            mutableStateListOf()
        }
    }

    // Track the currently active item
    val activeItemIndex = remember { mutableIntStateOf(0) }

    // Track whether the mic is active
    val isMicActive = remember { mutableStateOf(false) }

    // Function to find the first unchecked item
    val findFirstUnchecked = {
        checklistItems.indices.firstOrNull { it !in completedItems }
    }

    // Try to restore saved state if resuming
    LaunchedEffect(checklistData, resumeFromSaved) {
        if (checklistData != null && resumeFromSaved) {
            val savedState = stateManager.getChecklistState(checklistName)

            if (savedState != null) {
                // Restore section and list indices
                selectedSectionIndex.intValue = savedState.sectionIndex
                selectedListIndex.intValue = savedState.listIndex
                activeItemIndex.intValue = savedState.activeItemIndex

                // Restore completed items
                try {
                    savedState.completedItems.forEachIndexed { sectionIdx, sectionLists ->
                        sectionLists.forEachIndexed { listIdx, completedIndices ->
                            completedItemsBySection[sectionIdx][listIdx].clear()
                            completedItemsBySection[sectionIdx][listIdx].addAll(completedIndices)
                        }
                    }
                } catch (_: Exception) {
                    // Handle potential index out of bounds if checklist structure changed
                }
            }
        }
    }

    // Save state when component is disposed or when values change
    LaunchedEffect(
        selectedSectionIndex.intValue,
        selectedListIndex.intValue,
        activeItemIndex.intValue
    ) {
        if (checklistData != null) {
            // Convert completedItemsBySection to a regular List for serialization
            val serializedCompletedItems = completedItemsBySection.map { sectionLists ->
                sectionLists.map { it.toList() }
            }

            val state = ChecklistState(
                checklistFilename = checklistName,
                sectionIndex = selectedSectionIndex.intValue,
                listIndex = selectedListIndex.intValue,
                activeItemIndex = activeItemIndex.intValue,
                completedItems = serializedCompletedItems
            )

            stateManager.saveChecklistState(state)
        }
    }

    // Helper function to handle task completion logic
    val handleTaskCompletion: (Int) -> Unit = { itemIndex ->
        if (itemIndex < checklistItems.size) {
            if (itemIndex !in completedItems) {
                completedItems.add(itemIndex)
                // Move to next item if available
                findFirstUnchecked()?.let {
                    activeItemIndex.intValue = it
                }
            } else {
                // If the item is already completed, un-complete it
                completedItems.remove(itemIndex)
            }
        }
    }

    // Helper function to handle navigation home
    val handleNavigateHome = {
        onNavigateHome()
    }

    // Helper function to handle skipping to the next item
    val handleSkipItem: () -> Unit = {
        // Skip the current item and move to next without confirmation
        val nextUncheckedItem =
            checklistItems.indices.firstOrNull { it > activeItemIndex.intValue && it !in completedItems }
                ?: activeItemIndex.intValue
        activeItemIndex.intValue = nextUncheckedItem
    }

    // Helper function to find and navigate to skipped items
    val handleSearchItem: () -> Unit = {
        // Find the first skipped item (items that are not in completedItems)
        val firstSkipped = checklistItems.indices.firstOrNull {
            it !in completedItems && it != activeItemIndex.intValue
        }
        // If found, navigate to it
        firstSkipped?.let {
            activeItemIndex.intValue = it
        }
    }

    // Helper function to toggle microphone state
    val handleToggleMic: () -> Unit = {
        isMicActive.value = !isMicActive.value
    }

    // Helper function to handle emergency actions
    val handleEmergency: () -> Unit = {
        // Action to display emergency checklists will go here
        // TODO: Implement emergency action functionality
    }

    // Track whether we're showing tiles or a list in tile view mode
    val showingTileGrid = remember { mutableStateOf(true) }

    // When section changes, reset to showing tiles in tile view
    LaunchedEffect(selectedSectionIndex.intValue) {
        if (currentViewMode == "tileListView") {
            showingTileGrid.value = true
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopBar()
            }
        },
        bottomBar = {
            Column {
                // Display ListSelector only when in list view and there's more than one list
                if (currentViewMode == "normalListView" && currentSectionLists.size > 1) {
                    ListSelector(
                        lists = currentSectionLists,
                        selectedListIndex = selectedListIndex.intValue,
                        onListSelected = { newIndex ->
                            selectedListIndex.intValue = newIndex
                            // Reset active item when changing lists
                            activeItemIndex.intValue = 0
                        },
                        isNormalListView = true,
                        completedItemsByList = completedItemsBySection[selectedSectionIndex.intValue]
                    )
                }

                // Place the SectionSelector below the ListSelector, but above the ChecklistBottomRibbon
                if (checklistData != null && checklistData.sections.size > 1) {
                    SectionSelector(
                        sections = checklistData.sections,
                        selectedSectionIndex = selectedSectionIndex.intValue,
                        onSectionSelected = { newIndex ->
                            val newSectionType = checklistData.sections[newIndex].sectionType.lowercase()
                            if (newSectionType == "emergency" || newSectionType == "reference") {
                                // Reset to tile view when selecting emergency or reference sections
                                showingTileGrid.value = true
                            }

                            selectedSectionIndex.intValue = newIndex
                            // Reset selected list index when changing sections
                            selectedListIndex.intValue = 0
                            // Reset active item when changing sections
                            activeItemIndex.intValue = 0
                        }
                    )
                }

                ChecklistBar(
                    onNavigateHome = handleNavigateHome,
                    onCheckItem = { handleTaskCompletion(activeItemIndex.intValue) },
                    onSkipItem = handleSkipItem,
                    onSearchItem = handleSearchItem,
                    onToggleMic = handleToggleMic,
                    onEmergency = handleEmergency,
                    isMicActive = isMicActive.value,
                    isActiveItemEnabled = activeItemIndex.intValue < checklistItems.size
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(JarvisTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Checklist title
            Text(
                text = checklistData?.title ?: checklistName,
                style = JarvisTheme.typography.headlineMedium,
                color = JarvisTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // Section title if available
            if (checklistData != null && selectedSectionIndex.intValue < checklistData.sections.size) {
                val sectionTitle =
                    checklistData.sections[selectedSectionIndex.intValue].sectionTitle
                if (sectionTitle.isNotEmpty()) {
                    Text(
                        text = sectionTitle,
                        style = JarvisTheme.typography.titleMedium,
                        color = JarvisTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Display current list title only in list view mode or when showing a list in tile mode
            if ((currentViewMode == "normalListView" ||
                        (currentViewMode == "tileListView" && !showingTileGrid.value)) &&
                currentSectionLists.isNotEmpty() &&
                selectedListIndex.intValue < currentSectionLists.size
            ) {
                val listTitle = currentSectionLists[selectedListIndex.intValue].listTitle
                if (listTitle.isNotEmpty()) {
                    // Create a function to handle marking all items as complete
                    val handleMarkAllComplete = {
                        // Add all indices to completedItems list - ensure we're using current list's items
                        val currentCompletedItems =
                            completedItemsBySection[selectedSectionIndex.intValue][selectedListIndex.intValue]
                        val itemIndices = checklistItems.indices
                        itemIndices.forEach { index ->
                            if (index !in currentCompletedItems) {
                                currentCompletedItems.add(index)
                            }
                        }
                    }

                    // Using ClickableListTitle for the list name
                    ClickableListTitle(
                        title = listTitle,
                        onClick = {
                            // Future TTS functionality will go here
                            // For now we'll leave it empty
                        },
                        onLongClick = handleMarkAllComplete,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Choose between list view and tile view based on the section's listView property
            when (currentViewMode) {
                "normalListView" -> {
                    // Normal list view using the new composable
                    ChecklistListView(
                        checklistItems = checklistItems,
                        completedItems = completedItems,
                        activeItemIndex = activeItemIndex.intValue,
                        onItemClick = { index -> activeItemIndex.intValue = index },
                        onToggleComplete = { index ->
                            handleTaskCompletion(index)
                        }
                    )
                }

                "tileListView" -> {
                    if (showingTileGrid.value) {
                        // Show tile grid with all lists
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentSectionLists) { list ->
                                val listIndex = currentSectionLists.indexOf(list)
                                // Get the current section type to apply the correct theme
                                val sectionType =
                                    checklistData?.sections?.get(selectedSectionIndex.intValue)?.sectionType
                                        ?: ""

                                ChecklistTile(
                                    checklistList = list,
                                    onTileClick = {
                                        selectedListIndex.intValue = listIndex
                                        // Switch from tile grid to list view
                                        showingTileGrid.value = false
                                        // Reset active item when changing lists
                                        activeItemIndex.intValue = 0
                                    },
                                    category = sectionType // Pass the section
                                )
                            }
                        }
                    } else {
                        JarvisIconButton(
                            onClick = {
                                // Go back to tile grid view
                                showingTileGrid.value = true
                            },
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            text = "Back to categories",
                            modifier = Modifier.padding(top = 8.dp)
                        )                        // Show individual list view when a tile has been selected

                        ChecklistListView(
                            checklistItems = checklistItems,
                            completedItems = completedItems,
                            activeItemIndex = activeItemIndex.intValue,
                            onItemClick = { index -> activeItemIndex.intValue = index },
                            onToggleComplete = { index ->
                                handleTaskCompletion(index)
                            }
                        )
                    }
                }

                else -> {
                    // Fallback to normal list view if listView property is not recognized
                    ChecklistListView(
                        checklistItems = checklistItems,
                        completedItems = completedItems,
                        activeItemIndex = activeItemIndex.intValue,
                        onItemClick = { index -> activeItemIndex.intValue = index },
                        onToggleComplete = { index ->
                            handleTaskCompletion(index)
                        }
                    )
                }
            }
        }
    }
}

