package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import site.jarviscopilot.jarvis.ui.components.ChecklistItemType
import site.jarviscopilot.jarvis.ui.components.JarvisChecklistItem
import site.jarviscopilot.jarvis.ui.components.JarvisConfirmationDialog
import site.jarviscopilot.jarvis.ui.components.JarvisIconButton
import site.jarviscopilot.jarvis.ui.components.TopRibbon
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.ui.components.ChecklistBottomRibbon

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

    // Extract checklist items from the loaded data
    // If data couldn't be loaded, provide fallback empty list
    val checklistItems = remember(checklistData) {
        if (checklistData != null && checklistData.sections.isNotEmpty()) {
            val firstSection = checklistData.sections.first()
            if (firstSection.lists.isNotEmpty()) {
                firstSection.lists.first().listItems
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Track which items are completed
    val completedItems = remember { mutableStateListOf<Int>() }
    // Track the currently active item
    val activeItemIndex = remember { mutableIntStateOf(0) }
    // Track whether the mic is active
    val isMicActive = remember { mutableStateOf(false) }
    // Track if a dialog is showing
    val showDialog = remember { mutableStateOf(false) }
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
                    showDialog.value = true
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

            // Checklist items
            LazyColumn {
                itemsIndexed(checklistItems) { index, item ->
                    JarvisChecklistItem(
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

        // Skip confirmation dialog
        if (showDialog.value) {
            JarvisConfirmationDialog(
                title = "Skip Item",
                message = "Are you sure you want to skip this item?",
                onConfirmClick = {
                    // Skip the current item and move to next
                    val nextUncheckedItem =
                        checklistItems.indices.firstOrNull { it > activeItemIndex.intValue && it !in completedItems }
                            ?: activeItemIndex.intValue
                    activeItemIndex.intValue = nextUncheckedItem
                    showDialog.value = false
                },
                onDismissClick = {
                    showDialog.value = false
                },
                onDismissRequest = {
                    showDialog.value = false
                },
                confirmText = "Skip",
                dismissText = "Cancel"
            )
        }
    }
}

/**
 * A composable that displays the ChecklistScreen with mock data for previews
 */
@Composable
fun ChecklistScreenPreview(
    darkTheme: Boolean = false,
    checklistName: String = "Pre-Flight Checklist"
) {
    // Create mock checklist data
    val mockChecklistData = remember {
        ChecklistData(
            title = checklistName,
            titleAudio = "", // Add required titleAudio field
            description = "A sample checklist for previewing the UI",
            sections = listOf(
                ChecklistSection(
                    sectionType = "standard", // Add required sectionType field
                    sectionTitle = "Main Section",
                    sectionTitleAudio = "", // Add required sectionTitleAudio field
                    sectionSelectorName = "Main", // Add required sectionSelectorName field
                    defaultView = "list", // Add required defaultView field
                    lists = listOf(
                        ChecklistList(
                            listTitle = "Critical Items",
                            listTitleAudio = "", // Add required listTitleAudio field
                            listSelectorName = "Critical", // Add required listSelectorName field
                            listItems = listOf(
                                ChecklistItem(
                                    listItemType = "standard", // Add required listItemType field
                                    challenge = "Check fuel level",
                                    challengeAudio = "", // Add required challengeAudio field
                                    response = "Minimum 30% required",
                                    responseAudio = "", // Add required responseAudio field
                                    mandatory = true,
                                    suppressAudioChallenge = false, // Add required suppressAudioChallenge field
                                    suppressAudioResponse = false // Add required suppressAudioResponse field
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
                                ),
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
                                ),
                                ChecklistItem(
                                    listItemType = "standard",
                                    challenge = "Communication check",
                                    challengeAudio = "",
                                    response = "All channels clear",
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
    val completedItems = remember { mutableStateListOf<Int>(0, 2) }
    val activeItemIndex = remember { mutableIntStateOf(1) }
    val isMicActive = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }

    JarvisTheme(darkTheme = darkTheme) {
        Scaffold(
            topBar = {
                Column {
                    TopRibbon()
                }
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        JarvisIconButton(
                            icon = Icons.Default.Home,
                            onClick = {}
                        )
                        JarvisIconButton(
                            icon = Icons.Default.Check,
                            onClick = {}
                        )
                        JarvisIconButton(
                            icon = Icons.Default.SkipNext,
                            onClick = { showDialog.value = true }
                        )

                        // Search button - find first skipped item
                        JarvisIconButton(
                            icon = Icons.Default.Search,
                            onClick = {}
                        )

                        JarvisIconButton(
                            icon = Icons.Default.Mic,
                            onClick = { isMicActive.value = !isMicActive.value },
                            iconTint = if (isMicActive.value)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.onPrimary
                        )

                        // Emergency button - displays emergency checklists
                        JarvisIconButton(
                            icon = Icons.Default.Warning,
                            onClick = { },
                            iconTint = MaterialTheme.colorScheme.error,
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    }
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

                // Checklist items
                val checklistItems = mockChecklistData.sections.first().lists.first().listItems
                LazyColumn {
                    itemsIndexed(checklistItems) { index, item ->
                        JarvisChecklistItem(
                            text = "${item.challenge}: ${item.response}",
                            isCompleted = index in completedItems,
                            type = if (item.mandatory) ChecklistItemType.WARNING else ChecklistItemType.NORMAL,
                            isActive = index == activeItemIndex.intValue,
                            onItemClick = {}
                        )
                    }
                }
            }

            // Skip confirmation dialog
            if (showDialog.value) {
                JarvisConfirmationDialog(
                    title = "Skip Item",
                    message = "Are you sure you want to skip this item?",
                    onConfirmClick = { showDialog.value = false },
                    onDismissClick = { showDialog.value = false },
                    onDismissRequest = { showDialog.value = false },
                    confirmText = "Skip",
                    dismissText = "Cancel"
                )
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
