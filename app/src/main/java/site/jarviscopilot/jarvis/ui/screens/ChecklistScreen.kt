package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import site.jarviscopilot.jarvis.ui.components.ChecklistBottomRibbon
import site.jarviscopilot.jarvis.ui.components.ChecklistItemType
import site.jarviscopilot.jarvis.ui.components.ChecklistItem
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.TopRibbon
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme

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

    // Extract checklist items from the loaded data based on selected section
    val checklistItems = remember(checklistData, selectedSectionIndex.intValue) {
        if (checklistData != null && checklistData.sections.isNotEmpty()) {
            if (selectedSectionIndex.intValue < checklistData.sections.size) {
                val section = checklistData.sections[selectedSectionIndex.intValue]
                if (section.lists.isNotEmpty()) {
                    section.lists.first().listItems
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Track which items are completed (per section)
    val completedItemsBySection = remember(checklistData) {
        if (checklistData != null) {
            List(checklistData.sections.size) { mutableStateListOf<Int>() }
        } else {
            listOf(mutableStateListOf<Int>())
        }
    }

    // Current section's completed items
    val completedItems = remember(selectedSectionIndex.intValue, completedItemsBySection) {
        if (selectedSectionIndex.intValue < completedItemsBySection.size) {
            completedItemsBySection[selectedSectionIndex.intValue]
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

    Scaffold(
        topBar = {
            Column {
                TopRibbon()
            }
        },
        bottomBar = {
            Column {
                // Place the SectionSelector above the ChecklistBottomRibbon
                if (checklistData != null && checklistData.sections.size > 1) {
                    SectionSelector(
                        sections = checklistData.sections,
                        selectedSectionIndex = selectedSectionIndex.intValue,
                        onSectionSelected = { newIndex ->
                            selectedSectionIndex.intValue = newIndex
                            // Reset active item when changing sections
                            activeItemIndex.intValue = 0
                        }
                    )
                }

                ChecklistBottomRibbon(
                    onNavigateHome = onNavigateHome,
                    onCheckItem = {
                        if (activeItemIndex.intValue < checklistItems.size &&
                            activeItemIndex.intValue !in completedItems
                        ) {
                            completedItems.add(activeItemIndex.intValue)
                            // Move to next item if available
                            findFirstUnchecked()?.let {
                                activeItemIndex.intValue = it
                            }
                        }
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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Checklist title
            Text(
                text = checklistData?.title ?: checklistName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Section title if available
            if (checklistData != null && selectedSectionIndex.intValue < checklistData.sections.size) {
                val sectionTitle = checklistData.sections[selectedSectionIndex.intValue].sectionTitle
                if (sectionTitle.isNotEmpty()) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Checklist items
            LazyColumn {
                itemsIndexed(checklistItems) { index, item ->
                    ChecklistItem(
                        text = "${item.challenge}: ${item.response}",
                        isCompleted = index in completedItems,
                        type = if (item.mandatory) ChecklistItemType.WARNING else ChecklistItemType.NORMAL,
                        isActive = index == activeItemIndex.intValue,
                        onItemClick = {
                            activeItemIndex.intValue = index
                        }
                    )
                }
            }
        }
    }
}

// A composable that displays the ChecklistScreen with mock data for previews
@Composable
fun ChecklistScreenPreview(
    darkTheme: Boolean = false,
    checklistName: String = "Pre-Flight Checklist"
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
                    listView = "list",
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
                        )
                    )
                ),
                ChecklistSection(
                    sectionType = "standard",
                    sectionTitle = "Flight Section",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Flight",
                    listView = "list",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Flight Items",
                            listTitleAudio = "",
                            listSelectorName = "Flight",
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
                                ),
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Weather conditions",
                                    challengeAudio = "",
                                    response = "Within acceptable parameters",
                                    responseAudio = "",
                                    mandatory = true,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                )
                            )
                        )
                    )
                ),
                ChecklistSection(
                    sectionType = "standard",
                    sectionTitle = "Post-flight Section",
                    sectionTitleAudio = "",
                    sectionSelectorName = "Post-flight",
                    listView = "list",
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Post-flight Items",
                            listTitleAudio = "",
                            listSelectorName = "Post-flight",
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Communication check",
                                    challengeAudio = "",
                                    response = "All channels clear",
                                    responseAudio = "",
                                    mandatory = false,
                                    suppressAudioChallenge = false,
                                    suppressAudioResponse = false
                                ),
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Engine shutdown",
                                    challengeAudio = "",
                                    response = "Complete shutdown procedure",
                                    responseAudio = "",
                                    mandatory = true,
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
    val selectedSectionIndex = remember { mutableIntStateOf(0) }

    JarvisTheme(darkTheme = darkTheme) {
        Scaffold(
            topBar = {
                Column {
                    TopRibbon()
                }
            },
            bottomBar = {
                Column {
                    // Add SectionSelector to the preview
                    SectionSelector(
                        sections = mockChecklistData.sections,
                        selectedSectionIndex = selectedSectionIndex.intValue,
                        onSectionSelected = { selectedSectionIndex.intValue = it }
                    )

                    ChecklistBottomRibbon(
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
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // Checklist title
                Text(
                    text = mockChecklistData.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Section title
                val currentSection = mockChecklistData.sections[selectedSectionIndex.intValue]
                Text(
                    text = currentSection.sectionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Checklist items - show items from the current section
                val checklistItems = currentSection.lists.first().listItems
                LazyColumn {
                    itemsIndexed(checklistItems) { index, item ->
                        ChecklistItem(
                            text = "${item.challenge}: ${item.response}",
                            isCompleted = index in completedItems,
                            type = if (item.mandatory) ChecklistItemType.WARNING else ChecklistItemType.NORMAL,
                            isActive = index == activeItemIndex.intValue,
                            onItemClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenLightPreview() {
    ChecklistScreenPreview(darkTheme = false)
}

@Preview(name = "Dark Mode", apiLevel = 35, showBackground = true)
@Composable
fun ChecklistScreenDarkPreview() {
    ChecklistScreenPreview(darkTheme = true)
}
