package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import site.jarviscopilot.jarvis.ui.components.ItemsList
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.ListSelector
import site.jarviscopilot.jarvis.ui.components.SectionHeader
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.theme.LocalAviationColors
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel
import site.jarviscopilot.jarvis.model.Checklist
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.model.ChecklistSection
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex

@Composable
fun ItemsDetailScreen(
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
                // List selector for the current section
                uiState.checklist?.sections?.getOrNull(uiState.selectedSectionIndex)?.let { section ->
                    ListSelector(
                        lists = section.lists,
                        selectedIndex = uiState.selectedListIndex,
                        onListSelected = { viewModel.selectList(it) }
                    )
                }

                // Section selector bar at the bottom
                uiState.checklist?.let { checklist ->
                    SectionSelector(
                        sections = checklist.sections,
                        selectedIndex = uiState.selectedSectionIndex,
                        onSectionSelected = { viewModel.selectSection(it) }
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
                    val previousListIndex = remember { mutableStateOf(currentListIndex) }
                    val previousItemIndex = remember { mutableStateOf(currentItemIndex) }

                    // Remember the header height for proper placement
                    val headerHeight = remember { mutableStateOf(90.dp) }

                    // Scroll to the selected item
                    LaunchedEffect(targetIndex, currentListIndex, currentItemIndex) {
                        // If we switched to a different list, scroll to show the header (index - currentItemIndex)
                        if (previousListIndex.value != currentListIndex) {
                            // Calculate the index of just the header of the current list
                            var headerIndex = 0
                            for (i in 0 until currentListIndex) {
                                headerIndex += 1 // List header
                                headerIndex += currentSection.lists[i].items.size
                                headerIndex += 1 // Spacer after list
                            }

                            previousListIndex.value = currentListIndex

                            // Position the selected item with enough space below header
                            listState.scrollToItem(
                                index = 0, // Scroll to the first spacer item
                                scrollOffset = 0
                            )

                            // Then move to the current item with proper offset
                            listState.scrollToItem(
                                index = currentItemIndex,
                                scrollOffset = 0
                            )
                        } else if (previousItemIndex.value != currentItemIndex) {
                            // If only the item changed, ensure it's positioned properly
                            listState.scrollToItem(
                                index = currentItemIndex,
                                scrollOffset = 0
                            )
                            previousItemIndex.value = currentItemIndex
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
                                    val backgroundColor = if (currentSection.type.equals("emergency", ignoreCase = true)) {
                                        aviationColors.avRed
                                    } else {
                                        aviationColors.headerBackground
                                    }

                                    SectionHeader(
                                        title = currentList.name,
                                        backgroundColor = backgroundColor,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                                    )
                                }
                            )
                        }

                        // Then render the list content in a new column that's offset from the top
                        Column {
                            // Add padding at the top to account for the header
                            Spacer(modifier = Modifier.height(headerHeight.value))

                            // Render the list with items starting below the header
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            ) {
                                // Get the current list
                                if (currentList != null) {
                                    // Show only items from the current list
                                    items(currentList.items.size) { itemIndex ->
                                        val item = currentList.items[itemIndex]
                                        val isSelected = itemIndex == currentItemIndex

                                        ItemsList(
                                            item = item,
                                            isSelected = isSelected,
                                            onClick = {
                                                // First select the item
                                                viewModel.selectItem(itemIndex)

                                                // Then toggle its checked state
                                                viewModel.toggleItemChecked(itemIndex)
                                            },
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            sectionType = currentSection.type // Pass section type
                                        )
                                    }
                                }
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

@Preview(showBackground = true)
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
                                )
                            )
                        ),
                        ChecklistList(
                            name = "Before Starting Engine",
                            nameAudio = "",
                            items = listOf(
                                ChecklistItem(
                                    type = "item",
                                    label1 = "Seats, Belts",
                                    label1Audio = "",
                                    label2 = "ADJUST, SECURE",
                                    label2Audio = "",
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
                                    label1 = "Airspeed",
                                    label1Audio = "",
                                    label2 = "80 KIAS",
                                    label2Audio = "",
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

                                ItemsList(
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

                                SectionHeader(
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
