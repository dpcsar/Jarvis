package site.jarviscopilot.jarvis.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.di.AppDependencies
import site.jarviscopilot.jarvis.ui.components.ChecklistBar
import site.jarviscopilot.jarvis.ui.components.ClickableListTitle
import site.jarviscopilot.jarvis.ui.components.JarvisIconButton
import site.jarviscopilot.jarvis.ui.components.ListSelector
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.components.checklist.ChecklistListView
import site.jarviscopilot.jarvis.ui.components.checklist.ChecklistTileView
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel

/**
 * The main screen for displaying and interacting with checklists.
 * This is the entry point for the checklist feature and orchestrates all the components.
 */
@Composable
fun ChecklistScreen(
    checklistName: String,
    onNavigateHome: () -> Unit,
    resumeFromSaved: Boolean = false
) {
    // Get the current context to access the application
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Create ViewModel using factory from AppDependencies
    val viewModelFactory = AppDependencies.provideChecklistViewModelFactory(
        application = application,
        checklistName = checklistName,
        resumeFromSaved = resumeFromSaved
    )

    val viewModel: ChecklistViewModel = viewModel(factory = viewModelFactory)

    // Observe the ViewModel's UI state
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column {
                // TopBar doesn't accept title, subtitle, or navigationIcon parameters
                TopBar()

                // Add a custom header with the checklist title and back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(JarvisTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    JarvisIconButton(
                        onClick = onNavigateHome,
                        icon = Icons.AutoMirrored.Filled.ArrowBack
                    )

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text = uiState.checklistTitle,
                            style = JarvisTheme.typography.titleLarge,
                            color = JarvisTheme.colorScheme.onPrimaryContainer
                        )

                        if (uiState.checklistData?.description?.isNotEmpty() == true) {
                            Text(
                                text = uiState.checklistData?.description ?: "",
                                style = JarvisTheme.typography.bodyMedium,
                                color = JarvisTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Get current section details for list selector logic
                val currentSection =
                    uiState.checklistData?.sections?.getOrNull(uiState.selectedSectionIndex)
                val hasMultipleLists = (currentSection?.lists?.size ?: 0) > 1
                val isTileListView = currentSection?.listView == "tileListView"

                // Display the list selector first (above section selector)
                if (hasMultipleLists && !isTileListView) {
                    val lists = currentSection?.lists ?: emptyList()

                    ListSelector(
                        lists = lists,
                        selectedListIndex = uiState.selectedListIndex,
                        onListSelected = { list ->
                            viewModel.selectList(list)
                        }
                    )
                }

                // Then display the section selector
                if (uiState.checklistData?.sections?.isNotEmpty() == true) {
                    SectionSelector(
                        sections = uiState.checklistData?.sections ?: emptyList(),
                        selectedSectionIndex = uiState.selectedSectionIndex,
                        onSectionSelected = { section ->
                            viewModel.selectSection(section)
                        }
                    )
                }

                // Finally, the ChecklistBar
                ChecklistBar(
                    onNavigateHome = onNavigateHome,
                    onCheckItem = { viewModel.toggleItemCompletion(uiState.activeItemIndex) },
                    onSkipItem = { viewModel.skipItem() },
                    onSearchItem = { viewModel.searchItem() },
                    onSearchRequiredItem = { viewModel.searchRequiredItem() },
                    onToggleMic = { viewModel.toggleMic() },
                    onEmergency = { viewModel.selectFirstEmergencySection() },
                    isMicActive = uiState.isMicActive,
                    isActiveItemEnabled = uiState.activeItemIndex >= 0
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(JarvisTheme.colorScheme.background)
        ) {
            if (uiState.checklistData?.sections?.isEmpty() != false) {
                // If there are no sections, display a message
                Text(
                    text = "No checklist data available.",
                    style = JarvisTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            } else {
                // Add a visible spacer at the top of the content area
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .background(JarvisTheme.colorScheme.background)
                )

                // Display the current list based on section and list index
                val currentSection =
                    uiState.checklistData?.sections?.getOrNull(uiState.selectedSectionIndex)
                val currentList = currentSection?.lists?.getOrNull(uiState.selectedListIndex)

                if (currentList != null) {
                    // Only show list title when not in tile view mode
                    if (!uiState.showingTileGrid) {
                        // Display the list title
                        ClickableListTitle(
                            title = currentList.listTitle,
                            onClick = {
                                // Optional click action
                            },
                            onLongClick = {
                                viewModel.markAllItemsComplete()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Display the checklist items in either expanded or tile view
                    if (!uiState.showingTileGrid) {
                        // For list view, we use the individual items
                        ChecklistListView(
                            checklistItemData = currentList.listItems,
                            completedItems = uiState.completedItems,
                            activeItemIndex = uiState.activeItemIndex,
                            blockedTasks = uiState.blockedTasks,
                            onItemClick = { itemIndex ->
                                viewModel.selectChecklistItem(itemIndex)
                            },
                            onToggleComplete = { itemIndex ->
                                viewModel.toggleItemCompletion(itemIndex)
                            }
                        )
                    } else {
                        // For tile view, we need to pass the entire list and section type
                        val currentSectionType = currentSection.sectionType
                        // Use all lists from the current section instead of just the current list
                        val allListsInSection = currentSection.lists

                        ChecklistTileView(
                            lists = allListsInSection,  // Show all lists in the section
                            sectionType = currentSectionType,
                            onTileClick = { listIndex ->
                                // When a tile is clicked, select that list and switch to list view
                                viewModel.selectList(listIndex)
                                viewModel.toggleTileGridView(false)
                            }
                        )
                    }
                }
            }
        }
    }
}
