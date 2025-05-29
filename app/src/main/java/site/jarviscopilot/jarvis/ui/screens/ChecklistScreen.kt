package site.jarviscopilot.jarvis.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import site.jarviscopilot.jarvis.ui.components.ChecklistBar
import site.jarviscopilot.jarvis.ui.components.ClickableListTitle
import site.jarviscopilot.jarvis.ui.components.JarvisIconButton
import site.jarviscopilot.jarvis.ui.components.ListSelector
import site.jarviscopilot.jarvis.ui.components.SectionSelector
import site.jarviscopilot.jarvis.ui.components.TopBar
import site.jarviscopilot.jarvis.ui.components.checklist.ChecklistGridView
import site.jarviscopilot.jarvis.ui.components.checklist.ChecklistListView
import site.jarviscopilot.jarvis.ui.theme.JarvisTheme
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModel
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModelFactory

/**
 * The main screen for displaying and interacting with checklists.
 * This is the entry point for the checklist feature and orchestrates all the components.
 */
@Composable
fun ChecklistScreen(
    checklistName: String,
    checklistRepository: IChecklistRepository,
    checklistStateManager: ChecklistStateManager,
    onNavigateHome: () -> Unit,
    resumeFromSaved: Boolean = false
) {
    // Create ViewModel using factory with injected dependencies
    val viewModel: ChecklistViewModel = viewModel(
        factory = ChecklistViewModelFactory(
            repository = checklistRepository,
            stateManager = checklistStateManager,
            checklistName = checklistName,
            resumeFromSaved = resumeFromSaved
        )
    )

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Save state when screen is disposed
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.saveCurrentState()
        }
    }

    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Display ListSelector only when in list view and there's more than one list
                if (uiState.currentViewMode == "normalListView" && uiState.hasMultipleLists) {
                    ListSelector(
                        lists = uiState.currentSectionLists,
                        selectedListIndex = uiState.selectedListIndex,
                        onListSelected = { newIndex -> viewModel.selectList(newIndex) },
                        isNormalListView = true,
                        completedItemsByList = uiState.completedItemsBySection[uiState.selectedSectionIndex]
                    )
                }

                // Place the SectionSelector below the ListSelector, but above the ChecklistBottomRibbon
                if (uiState.hasMultipleSections) {
                    SectionSelector(
                        sections = uiState.checklistData?.sections ?: emptyList(),
                        selectedSectionIndex = uiState.selectedSectionIndex,
                        onSectionSelected = { newIndex -> viewModel.selectSection(newIndex) }
                    )
                }

                ChecklistBar(
                    onNavigateHome = onNavigateHome,
                    onCheckItem = { viewModel.toggleItemCompletion(uiState.activeItemIndex) },
                    onSkipItem = { viewModel.skipItem() },
                    onSearchItem = { viewModel.searchItem() },
                    onSearchRequiredItem = { viewModel.searchRequiredItem() },
                    onToggleMic = { viewModel.toggleMic() },
                    onEmergency = { viewModel.selectFirstEmergencySection() },
                    isMicActive = uiState.isMicActive,
                    isActiveItemEnabled = uiState.activeItemIndex < uiState.checklistItemData.size
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(JarvisTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Main Checklist title (no onLongClick functionality)
            Text(
                text = uiState.checklistTitle.ifEmpty { checklistName },
                style = JarvisTheme.typography.headlineMedium,
                color = JarvisTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Section title if available
            uiState.checklistData?.let { data ->
                if (uiState.selectedSectionIndex < data.sections.size) {
                    val currentSection = data.sections[uiState.selectedSectionIndex]
                    Text(
                        text = currentSection.sectionTitle,
                        style = JarvisTheme.typography.titleMedium,
                        color = JarvisTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                }
            }

            // List title with onLongClick to mark all items complete
            if (uiState.currentSectionLists.isNotEmpty() &&
                uiState.selectedListIndex < uiState.currentSectionLists.size &&
                (uiState.currentViewMode != "tileListView" || !uiState.showingTileGrid)
            ) {

                val currentList = uiState.currentSectionLists[uiState.selectedListIndex]
                ClickableListTitle(
                    title = currentList.listTitle,
                    onClick = { /* No action on normal click */ },
                    onLongClick = { viewModel.markAllItemsComplete() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }

            // Render different view based on current view mode
            when (uiState.currentViewMode) {
                // Normal list view - just show the checklist items
                "normalListView" -> {
                    ChecklistListView(
                        checklistItemData = uiState.checklistItemData,
                        completedItems = uiState.completedItems,
                        activeItemIndex = uiState.activeItemIndex,
                        onItemClick = { index -> viewModel.selectChecklistItem(index) },
                        onToggleComplete = { index -> viewModel.toggleItemCompletion(index) }
                    )
                }

                // Tile view - show either a grid of tiles or a list based on user selection
                "tileListView" -> {
                    if (uiState.showingTileGrid) {
                        // Show grid of tiles
                        ChecklistGridView(
                            lists = uiState.currentSectionLists,
                            sectionType = uiState.currentSectionType,
                            onTileClick = { listIndex ->
                                viewModel.selectList(listIndex)
                                viewModel.toggleTileGridView(false) // Switch to list view
                            }
                        )
                    } else {
                        // Back button
                        JarvisIconButton(
                            onClick = { viewModel.toggleTileGridView(true) },
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            text = "Back to categories",
                            modifier = Modifier.padding(top = 2.dp, bottom = 2.dp)
                        )

                        // Show individual list view when a tile has been selected
                        ChecklistListView(
                            checklistItemData = uiState.checklistItemData,
                            completedItems = uiState.completedItems,
                            activeItemIndex = uiState.activeItemIndex,
                            onItemClick = { index -> viewModel.selectChecklistItem(index) },
                            onToggleComplete = { index -> viewModel.toggleItemCompletion(index) }
                        )
                    }
                }

                else -> {
                    // Fallback to normal list view if listView property is not recognized
                    ChecklistListView(
                        checklistItemData = uiState.checklistItemData,
                        completedItems = uiState.completedItems,
                        activeItemIndex = uiState.activeItemIndex,
                        onItemClick = { index -> viewModel.selectChecklistItem(index) },
                        onToggleComplete = { index -> viewModel.toggleItemCompletion(index) }
                    )
                }
            }
        }
    }
}
