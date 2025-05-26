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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import site.jarviscopilot.jarvis.data.ChecklistData
import site.jarviscopilot.jarvis.data.ChecklistItem
import site.jarviscopilot.jarvis.data.ChecklistList
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.data.ChecklistSection
import site.jarviscopilot.jarvis.ui.components.ChecklistBar
import site.jarviscopilot.jarvis.ui.components.ChecklistItem
import site.jarviscopilot.jarvis.ui.components.ChecklistItemType
import site.jarviscopilot.jarvis.ui.components.ChecklistTile
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

    // Auto-scroll to active item when it changes
    LaunchedEffect(activeItemIndex) {
        if (checklistItems.isNotEmpty()) {
            // Get the item's layout info
            listState.layoutInfo.visibleItemsInfo.find { it.index == activeItemIndex }?.let { itemInfo ->
                // Calculate the center position of the viewport
                val viewportCenter = (listState.layoutInfo.viewportEndOffset + listState.layoutInfo.viewportStartOffset) / 2

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
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current

    // Create a ChecklistRepository instance
    val repository = remember { ChecklistRepository(context) }

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
            selectedSectionIndex.intValue < checklistData.sections.size) {
            checklistData.sections[selectedSectionIndex.intValue].listView
        } else {
            "normalListView" // Default to normal list view
        }
    }

    // Get lists from the selected section
    val currentSectionLists = remember(checklistData, selectedSectionIndex.intValue) {
        if (checklistData != null &&
            selectedSectionIndex.intValue < checklistData.sections.size) {
            checklistData.sections[selectedSectionIndex.intValue].lists
        } else {
            emptyList()
        }
    }

    // Extract checklist items from the loaded data based on selected section and list
    val checklistItems = remember(checklistData, selectedSectionIndex.intValue, selectedListIndex.intValue) {
        if (checklistData != null &&
            selectedSectionIndex.intValue < checklistData.sections.size &&
            selectedListIndex.intValue < checklistData.sections[selectedSectionIndex.intValue].lists.size) {
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
    val completedItems = remember(selectedSectionIndex.intValue, selectedListIndex.intValue, completedItemsBySection) {
        if (selectedSectionIndex.intValue < completedItemsBySection.size &&
            selectedListIndex.intValue < completedItemsBySection[selectedSectionIndex.intValue].size) {
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

    // Helper function to handle task completion logic
    val handleTaskCompletion = { itemIndex: Int ->
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
                        }
                    )
                }

                // Place the SectionSelector below the ListSelector, but above the ChecklistBottomRibbon
                if (checklistData != null && checklistData.sections.size > 1) {
                    SectionSelector(
                        sections = checklistData.sections,
                        selectedSectionIndex = selectedSectionIndex.intValue,
                        onSectionSelected = { newIndex ->
                            selectedSectionIndex.intValue = newIndex
                            // Reset selected list index when changing sections
                            selectedListIndex.intValue = 0
                            // Reset active item when changing sections
                            activeItemIndex.intValue = 0
                        }
                    )
                }

                ChecklistBar(
                    onNavigateHome = onNavigateHome,
                    onCheckItem = {
                        handleTaskCompletion(activeItemIndex.intValue)
                    },
                    onSkipItem = {
                        // Skip the current item and move to next without confirmation
                        val nextUncheckedItem =
                            checklistItems.indices.firstOrNull { it > activeItemIndex.intValue && it !in completedItems }
                                ?: activeItemIndex.intValue
                        activeItemIndex.intValue = nextUncheckedItem
                    },
                    onSearchItem = {
                        // Find the first skipped item (items that are not in completedItems)
                        val firstSkipped = checklistItems.indices.firstOrNull {
                            it !in completedItems && it != activeItemIndex.intValue
                        }
                        // If found, navigate to it
                        firstSkipped?.let {
                            activeItemIndex.intValue = it
                        }
                    },
                    onToggleMic = {
                        isMicActive.value = !isMicActive.value
                    },
                    onEmergency = {
                        // Action to display emergency checklists will go here
                    },
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Section title if available
            if (checklistData != null && selectedSectionIndex.intValue < checklistData.sections.size) {
                val sectionTitle = checklistData.sections[selectedSectionIndex.intValue].sectionTitle
                if (sectionTitle.isNotEmpty()) {
                    Text(
                        text = sectionTitle,
                        style = JarvisTheme.typography.titleMedium,
                        color = JarvisTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Display current list title only in list view mode or when showing a list in tile mode
            if ((currentViewMode == "normalListView" ||
                (currentViewMode == "tileListView" && !showingTileGrid.value)) &&
                currentSectionLists.isNotEmpty() &&
                selectedListIndex.intValue < currentSectionLists.size) {
                val listTitle = currentSectionLists[selectedListIndex.intValue].listTitle
                if (listTitle.isNotEmpty()) {
                    Text(
                        text = listTitle,
                        style = JarvisTheme.typography.titleSmall,
                        color = JarvisTheme.colorScheme.onBackground,
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
                                val sectionType = checklistData?.sections?.get(selectedSectionIndex.intValue)?.sectionType ?: ""

                                ChecklistTile(
                                    checklistList = list,
                                    onTileClick = {
                                        selectedListIndex.intValue = listIndex
                                        // Switch from tile grid to list view
                                        showingTileGrid.value = false
                                        // Reset active item when changing lists
                                        activeItemIndex.intValue = 0
                                    },
                                    category = sectionType // Pass the section type as the category
                                )
                            }
                        }
                    } else {
                        // Back button to return to tile grid
                        JarvisIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { showingTileGrid.value = true },
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = "Back to tiles"
                        )

                        // Show selected list using the new composable
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
}

// A composable that displays the ChecklistScreen with mock data for previews
@Composable
fun ChecklistScreenPreview(
    darkTheme: Boolean = false,
    checklistName: String = "Pre-Flight Checklist",
    previewSection: Int = 0 // 0 for normalListView, 1 for tileListView, 2 for emergency, 3 for reference
) {
    // Create mock checklist data with multiple sections
    val mockChecklistData = remember {
        ChecklistData(
            title = checklistName,
            titleAudio = "",
            description = "A sample checklist for previewing the UI",
            sections = listOf(
                ChecklistSection(
                    sectionType = "standard",
                    sectionTitle = "Pre-flight Section",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Pre-flight",
                    listView = "normalListView",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Critical Items",
                            listTitleAudio = "",
                            listSelectorName = "Critical",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Check fuel level",
                                    challengeAudio = "",
                                    response = "Minimum 30% required",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                ),
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Verify systems",
                                    challengeAudio = "",
                                    response = "All systems operational",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        ),
                        ChecklistList(
                            listTitle = "Secondary Items",
                            listTitleAudio = "",
                            listSelectorName = "Secondary",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Flight plan",
                                    challengeAudio = "",
                                    response = "Filed and approved",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                ),
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Passenger briefing",
                                    challengeAudio = "",
                                    response = "Completed",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        )
                    )
                ),
                ChecklistSection(
                    sectionType = "standard",
                    sectionTitle = "Flight Section",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Flight",
                    listView = "tileListView",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Takeoff Items",
                            listTitleAudio = "",
                            listSelectorName = "Takeoff",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Navigation",
                                    challengeAudio = "",
                                    response = "Route confirmed",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        ),
                        ChecklistList(
                            listTitle = "Cruise Items",
                            listTitleAudio = "",
                            listSelectorName = "Cruise",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Altitude check",
                                    challengeAudio = "",
                                    response = "Maintaining assigned altitude",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        ),
                        ChecklistList(
                            listTitle = "Landing Items",
                            listTitleAudio = "",
                            listSelectorName = "Landing",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Landing gear",
                                    challengeAudio = "",
                                    response = "Down and locked",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        )
                    )
                ),
                // Add emergency section
                ChecklistSection(
                    sectionType = "emergency",
                    sectionTitle = "Emergency Procedures",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Emergency",
                    listView = "tileListView",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Engine Failure",
                            listTitleAudio = "",
                            listSelectorName = "Engine",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "emergency",
                                    challenge = "Airspeed",
                                    challengeAudio = "",
                                    response = "Best glide speed",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        ),
                        ChecklistList(
                            listTitle = "Fire Procedures",
                            listTitleAudio = "",
                            listSelectorName = "Fire",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "emergency",
                                    challenge = "Fuel selector",
                                    challengeAudio = "",
                                    response = "Off",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        )
                    )
                ),
                // Add reference section
                ChecklistSection(
                    sectionType = "reference",
                    sectionTitle = "Reference Materials",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Reference",
                    listView = "tileListView",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Weather Codes",
                            listTitleAudio = "",
                            listSelectorName = "Weather",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "reference",
                                    challenge = "METAR",
                                    challengeAudio = "",
                                    response = "Current weather observation",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        ),
                        ChecklistList(
                            listTitle = "Radio Frequencies",
                            listTitleAudio = "",
                            listSelectorName = "Radio",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "reference",
                                    challenge = "Emergency frequency",
                                    challengeAudio = "",
                                    response = "121.5 MHz",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    // Mock state variables
    val completedItems = remember { mutableStateListOf<Int>(0) }
    val activeItemIndex = remember { mutableIntStateOf(1) }
    val isMicActive = remember { mutableStateOf(false) }
    val selectedSectionIndex = remember { mutableIntStateOf(previewSection) }
    val selectedListIndex = remember { mutableIntStateOf(0) }

    // Get current view mode from the selected section
    val currentViewMode = mockChecklistData.sections[selectedSectionIndex.intValue].listView
    // Get lists from the selected section
    val currentSectionLists = mockChecklistData.sections[selectedSectionIndex.intValue].lists

    JarvisTheme(darkTheme = darkTheme) {
        Scaffold(
            topBar = {
                Column {
                    TopBar()
                }
            },
            bottomBar = {
                Column {
                    // Display ListSelector only in normalListView mode with multiple lists
                    if (currentViewMode == "normalListView" && currentSectionLists.size > 1) {
                        ListSelector(
                            lists = currentSectionLists,
                            selectedListIndex = selectedListIndex.intValue,
                            onListSelected = { selectedListIndex.intValue = it }
                        )
                    }

                    // Add SectionSelector if we have multiple sections
                    SectionSelector(
                        sections = mockChecklistData.sections,
                        selectedSectionIndex = selectedSectionIndex.intValue,
                        onSectionSelected = { selectedSectionIndex.intValue = it }
                    )

                    ChecklistBar(
                        onNavigateHome = { },
                        onCheckItem = { },
                        onSkipItem = { },
                        onSearchItem = { },
                        onToggleMic = { isMicActive.value = !isMicActive.value },
                        onEmergency = { },
                        isMicActive = isMicActive.value,
                        isActiveItemEnabled = true
                    )
                }
            }
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
                    text = mockChecklistData.title,
                    style = JarvisTheme.typography.headlineMedium,
                    color = JarvisTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Section title
                val currentSection = mockChecklistData.sections[selectedSectionIndex.intValue]
                Text(
                    text = currentSection.sectionTitle,
                    style = JarvisTheme.typography.titleMedium,
                    color = JarvisTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Display list title only in normal list view mode
                if (currentViewMode == "normalListView" &&
                    selectedListIndex.intValue < currentSectionLists.size) {
                    val listTitle = currentSectionLists[selectedListIndex.intValue].listTitle
                    if (listTitle.isNotEmpty()) {
                        Text(
                            text = listTitle,
                            style = JarvisTheme.typography.titleSmall,
                            color = JarvisTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                when (currentViewMode) {
                    "normalListView" -> {
                        // Normal list view displaying checklist items
                        if (selectedListIndex.intValue < currentSectionLists.size) {
                            val checklistItems = currentSectionLists[selectedListIndex.intValue].listItems
                            ChecklistListView(
                                checklistItems = checklistItems,
                                completedItems = completedItems,
                                activeItemIndex = activeItemIndex.intValue,
                                onItemClick = {},
                                onToggleComplete = { index ->
                                    if (index in completedItems) {
                                        completedItems.remove(index)
                                    } else {
                                        completedItems.add(index)
                                    }
                                }
                            )
                        }
                    }
                    "tileListView" -> {
                        // Tile view showing grid of list tiles
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(currentSectionLists) { list ->
                                val listIndex = currentSectionLists.indexOf(list)
                                ChecklistTile(
                                    checklistList = list,
                                    onTileClick = {
                                        selectedListIndex.intValue = listIndex
                                    },
                                    category = when (currentSection.sectionType) {
                                        "emergency" -> "emergency"
                                        "reference" -> "reference"
                                        else -> ""
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenLightPreview() {
    ChecklistScreenPreview(darkTheme = false, previewSection = 0)
}

@Preview(name = "Dark Mode", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenDarkPreview() {
    ChecklistScreenPreview(darkTheme = true, previewSection = 0)
}

@Preview(name = "Light Mode Emergency Tiles", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenLightEmergencyPreview() {
    ChecklistScreenPreview(darkTheme = false, previewSection = 2)
}

@Preview(name = "Light Mode Emergency Tiles", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenDarkEmergencyPreview() {
    ChecklistScreenPreview(darkTheme = true, previewSection = 2)
}

@Preview(name = "Light Mode Reference Tiles", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenLightReferencePreview() {
    ChecklistScreenPreview(darkTheme = false, previewSection = 3)
}

@Preview(name = "Light Mode Reference Tiles", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenDarkReferencePreview() {
    ChecklistScreenPreview(darkTheme = true, previewSection = 3)
}
