package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.ui.components.BottomBar
import site.jarviscopilot.jarvis.ui.components.ListView
import site.jarviscopilot.jarvis.ui.components.Item
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.ListSelector
import site.jarviscopilot.jarvis.ui.components.ViewHeader
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel
import site.jarviscopilot.jarvis.model.Checklist
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.model.ChecklistSection
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.mutableIntStateOf
import site.jarviscopilot.jarvis.ui.components.ListTileView

@Composable
fun DetailsScreen(
    listIndex: Int,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChecklistViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val aviationColors = LocalAviationColors.current
    
    // Set the selected section when the screen is first displayed
    LaunchedEffect(key1 = listIndex) {
        viewModel.selectSection(listIndex)
    }
    
    // Track if we're in emergency/reference tile view
    val isEmergencyOrRef = uiState.checklist?.sections?.getOrNull(uiState.selectedSectionIndex)?.type?.equals("emergency", ignoreCase = true) == true ||
                           uiState.checklist?.sections?.getOrNull(uiState.selectedSectionIndex)?.type?.equals("reference", ignoreCase = true) == true

    // Track if we're viewing list tiles or a specific list - move to higher scope
    val isViewingListTiles = remember(uiState.selectedSectionIndex) { mutableStateOf(isEmergencyOrRef) }

    // Create a shared reference to track if we're viewing a specific list within emergency/reference
    val isViewingSpecificList = remember { mutableStateOf(false) }

    // Update tile view state when section type changes
    LaunchedEffect(isEmergencyOrRef) {
        isViewingListTiles.value = isEmergencyOrRef
        if (isEmergencyOrRef) {
            isViewingSpecificList.value = false // Reset to tile view when switching to emergency/reference
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                localTime = uiState.currentLocalTime,
                utcTime = uiState.currentUtcTime,
                onMenuClick = { /* Open menu - implement later */ }
            )
        },
        bottomBar = {
            Column {
                // Only show list selector when not in tile view mode
                if (!isEmergencyOrRef || !isViewingListTiles.value) {
                    // List selector for the current section
                    uiState.checklist?.sections?.getOrNull(uiState.selectedSectionIndex)?.let { section ->
                        ListSelector(
                            lists = section.lists,
                            selectedIndex = uiState.selectedListIndex,
                            onListSelected = {
                                viewModel.selectList(it)
                                if (isEmergencyOrRef) {
                                    isViewingListTiles.value = false
                                }
                            }
                        )
                    }
                }

                // Section selector bar at the bottom
                uiState.checklist?.let { checklist ->
                    SectionSelector(
                        sections = checklist.sections,
                        selectedIndex = uiState.selectedSectionIndex,
                        onSectionSelected = {
                            viewModel.selectSection(it)
                            // Normal sections don't force tile view
                        },
                        onSpecialSectionSelected = {
                            // Check if we're clicking the same section we're already on
                            val isSameSection = it == uiState.selectedSectionIndex

                            // If it's the same section and we're in emergency/reference,
                            // reset to tile view without changing the section
                            if (isSameSection && isEmergencyOrRef) {
                                // Reset to tile view (this will be applied even if already in tile view)
                                isViewingListTiles.value = true
                                isViewingSpecificList.value = false
                            } else {
                                // Different section, perform normal selection
                                viewModel.selectSection(it)
                                // Always force tile view for emergency/reference sections
                                isViewingListTiles.value = true
                                isViewingSpecificList.value = false
                            }
                        }
                    )
                }

                // Control buttons
                BottomBar(
                    onHomeClick = onNavigateUp,
                    onCheckClick = { viewModel.checkCurrentItem() },
                    onSkipClick = { viewModel.skipCurrentItem() },
                    onMicClick = { /* Implement voice feature later */ },
                    onRepeatClick = { /* Implement repeat feature later */ },
                    canSkip = true, // Could be based on mandatory status later
                    isListening = false // Will be true when listening for voice
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = aviationColors.avRed,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                val currentSection = uiState.checklist?.sections?.getOrNull(uiState.selectedSectionIndex)
                if (currentSection != null) {
                    val listState = rememberLazyListState()
                    val currentListIndex = uiState.selectedListIndex
                    val currentItemIndex = uiState.selectedItemIndex
                    val currentList = currentSection.lists.getOrNull(currentListIndex)

                    // Calculate the overall index for scrolling
                    val targetIndex by remember(currentListIndex, currentItemIndex) {
                        derivedStateOf {
                            var index = 0
                            for (i in 0 until currentListIndex) {
                                index += 1 // List header
                                index += currentSection.lists[i].items.size
                                index += 1 // Spacer after list
                            }
                            index += 1 // Current list header
                            index += currentItemIndex
                            
                            index
                        }
                    }
                    
                    // Track previous list index to detect list changes
                    val previousListIndex = remember { mutableIntStateOf(currentListIndex) }
                    val previousItemIndex = remember { mutableIntStateOf(currentItemIndex) }

                    // Remember the header height for proper placement
                    val headerHeight = remember { mutableStateOf(90.dp) }

                    // Scroll to the selected item, positioning it in the middle of the screen
                    LaunchedEffect(targetIndex, currentListIndex, currentItemIndex) {
                        // If we switched to a different list or the item changed, scroll to center the current item
                        if (previousListIndex.intValue != currentListIndex || previousItemIndex.intValue != currentItemIndex) {
                            previousListIndex.intValue = currentListIndex
                            previousItemIndex.intValue = currentItemIndex

                            // Center the current item on screen with an offset
                            listState.animateScrollToItem(
                                index = currentItemIndex,
                                scrollOffset = -listState.layoutInfo.viewportSize.height / 3 // Position item roughly in the middle
                            )
                        }
                    }
                    
                    // Use a box to position the sticky header separately from the list content
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Create the header first as a fixed element at the top
                        if (currentList != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(1f)
                                    .shadow(4.dp),
                                content = {
                                    // Use a different background color for emergency checklists
                                    val sectionType = when {
                                        currentSection.type.equals("emergency", ignoreCase = true) -> "emergency"
                                        currentSection.type.equals("reference", ignoreCase = true) -> "reference"
                                        else -> "normal"
                                    }

                                    // Determine the header title based on context
                                    val headerTitle = if ((currentSection.type.equals("emergency", ignoreCase = true) ||
                                                          currentSection.type.equals("reference", ignoreCase = true)) &&
                                                          isViewingListTiles.value) { // Show section name when in tile view mode
                                        // In tile view mode, show section name
                                        if (currentSection.type.equals("emergency", ignoreCase = true))
                                            "Emergency Procedures"
                                        else
                                            "Reference Information"
                                    } else {
                                        // In list view mode, show the list name
                                        currentList.name
                                    }

                                    ViewHeader(
                                        title = headerTitle,
                                        sectionType = sectionType,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                                    )
                                }
                            )
                        }

                        // Determine which view to show based on section type
                        Column {
                            // Add padding at the top to account for the header
                            Spacer(modifier = Modifier.height(headerHeight.value))

                            // Show tile view for emergency or reference sections, list view for others
                            if (currentSection.type.equals("emergency", ignoreCase = true) ||
                                currentSection.type.equals("reference", ignoreCase = true)) {

                                // Track if we're viewing a specific list or the list of lists
                                val isViewingList = remember { mutableStateOf(false) }
                                val selectedListIndex = remember { mutableIntStateOf(0) }

                                // Keep the local and shared states in sync
                                LaunchedEffect(isViewingSpecificList.value) {
                                    isViewingList.value = isViewingSpecificList.value
                                }

                                // And sync in the other direction too
                                LaunchedEffect(isViewingList.value) {
                                    isViewingSpecificList.value = isViewingList.value
                                }

                                // Store isViewingList in a higher scope so it can be reset by section selector
                                LaunchedEffect(uiState.selectedSectionIndex, isViewingListTiles.value) {
                                    // Reset to tile view when section changes or when isViewingListTiles is set to true
                                    if (isViewingListTiles.value) {
                                        isViewingList.value = false
                                        isViewingSpecificList.value = false
                                    }
                                }

                                if (!isViewingList.value) {
                                    // Show the tile view of all lists in this section
                                    ListTileView(
                                        lists = currentSection.lists,
                                        sectionType = currentSection.type,
                                        onListSelected = { index ->
                                            selectedListIndex.intValue = index
                                            isViewingList.value = true
                                            viewModel.selectList(index)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp)
                                    )
                                } else {
                                    // Show a specific list with its items and a back button
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        // Back button to return to list view
                                        Button(
                                            onClick = { isViewingList.value = false },
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(start = 8.dp, bottom = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back to lists"
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Back to lists")
                                        }

                                        // Show the items in the selected list
                                        LazyColumn(
                                            state = listState,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 60.dp, start = 8.dp, end = 8.dp)
                                        ) {
                                            if (currentList != null) {
                                                items(currentList.items.size) { itemIndex ->
                                                    val item = currentList.items[itemIndex]
                                                    val isSelected = itemIndex == currentItemIndex

                                                    Item(
                                                        item = item,
                                                        isSelected = isSelected,
                                                        onClick = {
                                                            viewModel.selectItem(itemIndex)
                                                            viewModel.toggleItemChecked()
                                                        },
                                                        modifier = Modifier.padding(vertical = 4.dp),
                                                        sectionType = currentSection.type
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Standard list view for normal checklists
                                ListView(
                                    list = currentList,
                                    currentItemIndex = currentItemIndex,
                                    onItemClick = { itemIndex ->
                                        viewModel.selectItem(itemIndex)
                                        viewModel.toggleItemChecked()
                                    },
                                    listState = listState,
                                    sectionType = currentSection.type
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "No checklist selected",
                        color = aviationColors.textOnBackground,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, apiLevel = 35)
@Composable
fun ChecklistDetailScreenPreview() {
    JarvisTheme {
        val mockChecklist = Checklist(
            name = "RV-6A",
            nameAudio = "R V nine two six romeo uniform",
            checklistId = "N926RU",
            description = "Checklist for RV-6A",
            sections = listOf(
                ChecklistSection(
                    type = "checklist",
                    name = "Preflight",
                    nameAudio = "",
                    defaultView = "checklistView",
                    lists = listOf(
                        ChecklistList(
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
                                )
                            )
                        ),
                        ChecklistList(
                            name = "Before Starting Engine",
                            nameAudio = "",
                            items = listOf(
                                ChecklistItem(
                                    type = "item",
                                    challenge = "Seats, Belts",
                                    challengeAudio = "",
                                    response = "ADJUST, SECURE",
                                    responseAudio = "",
                                    mandatory = true
                                )
                            )
                        )
                    )
                ),
                ChecklistSection(
                    type = "emergency",
                    name = "Emergency",
                    nameAudio = "",
                    defaultView = "onePageView",
                    lists = listOf(
                        ChecklistList(
                            name = "Engine Failure",
                            nameAudio = "",
                            items = listOf(
                                ChecklistItem(
                                    type = "item",
                                    challenge = "Airspeed",
                                    challengeAudio = "",
                                    response = "80 KIAS",
                                    responseAudio = "",
                                    mandatory = false
                                )
                            )
                        )
                    )
                )
            )
        )
        
        ChecklistDetailScreenPreviewContent(mockChecklist)
    }
}

@Composable
private fun ChecklistDetailScreenPreviewContent(checklist: Checklist) {
    val aviationColors = LocalAviationColors.current
    val currentSection = checklist.sections.getOrNull(0)
    val selectedSectionIndex = 0
    
    Scaffold(
        topBar = {
            TopBar(
                localTime = "12:34:56",
                utcTime = "16:34:56",
                onMenuClick = { }
            )
        },
        bottomBar = {
            Column {
                // List selector for the current section
                currentSection?.let { section ->
                    ListSelector(
                        lists = section.lists,
                        selectedIndex = 0,
                        onListSelected = { }
                    )
                }

                // Section selector bar at the bottom
                SectionSelector(
                    sections = checklist.sections,
                    selectedIndex = selectedSectionIndex,
                    onSectionSelected = { }
                )

                // Control buttons
                BottomBar(
                    onHomeClick = { },
                    onCheckClick = { },
                    onSkipClick = { },
                    onMicClick = { },
                    onRepeatClick = { },
                    canSkip = true,
                    isListening = false
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentSection != null) {
                val listState = rememberLazyListState()
                val currentListIndex = 0
                val currentItemIndex = 0
                
                Box(modifier = Modifier.fillMaxSize()) {
                    // First, render the list content
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        // Get the current list
                        val currentList = currentSection.lists.getOrNull(currentListIndex)

                        if (currentList != null) {
                            // Add space at the top for the sticky header to prevent items from being hidden behind it
                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                            
                            // Show only items from the current list
                            items(currentList.items.size) { itemIndex ->
                                val item = currentList.items[itemIndex]
                                val isSelected = itemIndex == currentItemIndex

                                Item(
                                    item = item,
                                    isSelected = isSelected,
                                    onClick = { /* No action in preview */ },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    sectionType = currentSection.type
                                )
                            }
                        }
                    }

                    // Then, render the sticky header on top
                    if (currentSection.lists.getOrNull(currentListIndex) != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f)
                                .shadow(4.dp),
                            content = {
                                val currentList = currentSection.lists.getOrNull(currentListIndex)
                                // Use a different background color for emergency checklists
                                val backgroundColor = if (currentSection.type.equals("emergency", ignoreCase = true)) {
                                    aviationColors.avRed
                                } else {
                                    aviationColors.headerBackground
                                }

                                ViewHeader(
                                    title = currentList?.name ?: "",
                                    backgroundColor = backgroundColor,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
