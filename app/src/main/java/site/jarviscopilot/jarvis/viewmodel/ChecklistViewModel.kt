package site.jarviscopilot.jarvis.viewmodel

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import site.jarviscopilot.jarvis.data.ChecklistData
import site.jarviscopilot.jarvis.data.ChecklistItem
import site.jarviscopilot.jarvis.data.ChecklistList
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.data.ChecklistState
import site.jarviscopilot.jarvis.data.ChecklistStateManager

/**
 * ViewModel for the Checklist screen that handles all business logic and state management.
 */
class ChecklistViewModel(
    private val repository: ChecklistRepository,
    private val stateManager: ChecklistStateManager,
    private val checklistName: String,
    resumeFromSaved: Boolean = false  // Changed from private val to parameter
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    // Checklist data
    val checklistData = mutableStateOf<ChecklistData?>(null)

    // Section and list selection
    val selectedSectionIndex = mutableIntStateOf(0)
    val selectedListIndex = mutableIntStateOf(0)
    val activeItemIndex = mutableIntStateOf(0)

    // UI state flags
    val isMicActive = mutableStateOf(false)
    val showingTileGrid = mutableStateOf(true)

    // Completed items tracking
    val completedItemsBySection = mutableStateListOf<List<MutableList<Int>>>()

    init {
        loadChecklistData()
        if (resumeFromSaved) {
            restoreSavedState()
        }
    }

    /**
     * Loads checklist data from repository
     */
    private fun loadChecklistData() {
        val data = repository.loadChecklist(checklistName)
        checklistData.value = data

        // Initialize completedItemsBySection
        completedItemsBySection.clear()
        data?.sections?.map { section ->
            section.lists.map { mutableStateListOf<Int>() }
        }?.let {
            completedItemsBySection.addAll(it)
        } ?: completedItemsBySection.add(listOf(mutableStateListOf()))

        // Update UI state
        updateUiState()
    }

    /**
     * Restores saved state if available
     */
    private fun restoreSavedState() {
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

            updateUiState()
        }
    }

    /**
     * Saves the current state
     */
    fun saveCurrentState() {
        checklistData.value?.let { data ->
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

    /**
     * Updates the UI state based on current selections
     */
    private fun updateUiState() {
        val data = checklistData.value ?: return

        if (selectedSectionIndex.intValue >= data.sections.size) {
            selectedSectionIndex.intValue = 0
        }

        val currentSection = data.sections[selectedSectionIndex.intValue]

        if (selectedListIndex.intValue >= currentSection.lists.size) {
            selectedListIndex.intValue = 0
        }

        val currentListView = currentSection.listView
        val currentSectionType = currentSection.sectionType
        val currentLists = currentSection.lists

        val currentList = if (selectedListIndex.intValue < currentLists.size) {
            currentLists[selectedListIndex.intValue]
        } else null

        val currentItems = currentList?.listItems ?: emptyList()

        val currentCompletedItems = if (
            selectedSectionIndex.intValue < completedItemsBySection.size &&
            selectedListIndex.intValue < completedItemsBySection[selectedSectionIndex.intValue].size
        ) {
            completedItemsBySection[selectedSectionIndex.intValue][selectedListIndex.intValue]
        } else {
            mutableStateListOf()
        }

        _uiState.update {
            it.copy(
                checklistTitle = data.title,
                currentViewMode = currentListView,
                currentSectionType = currentSectionType,
                currentSectionLists = currentLists,
                checklistItems = currentItems,
                completedItems = currentCompletedItems,
                hasMultipleSections = data.sections.size > 1,
                hasMultipleLists = currentLists.size > 1
            )
        }
    }

    /**
     * Handle selection of a section
     */
    fun selectSection(index: Int) {
        if (index == selectedSectionIndex.intValue) return

        val data = checklistData.value ?: return
        if (index >= 0 && index < data.sections.size) {
            val newSectionType = data.sections[index].sectionType.lowercase()
            if (newSectionType == "emergency" || newSectionType == "reference") {
                // Reset to tile view when selecting emergency or reference sections
                showingTileGrid.value = true
            }

            selectedSectionIndex.intValue = index
            // Reset selected list index when changing sections
            selectedListIndex.intValue = 0
            // Reset active item when changing sections
            activeItemIndex.intValue = 0

            updateUiState()
        }
    }

    /**
     * Handle selection of a list
     */
    fun selectList(index: Int) {
        if (index == selectedListIndex.intValue) return

        val data = checklistData.value ?: return
        val sectionIndex = selectedSectionIndex.intValue

        if (sectionIndex < data.sections.size) {
            val section = data.sections[sectionIndex]
            if (index >= 0 && index < section.lists.size) {
                selectedListIndex.intValue = index
                // Reset active item when changing lists
                activeItemIndex.intValue = 0

                updateUiState()
            }
        }
    }

    /**
     * Select a specific checklist item
     */
    fun selectChecklistItem(index: Int) {
        val items = uiState.value.checklistItems
        if (index >= 0 && index < items.size) {
            activeItemIndex.intValue = index
        }
    }

    /**
     * Toggle completion status of a checklist item
     */
    fun toggleCompleteItem(index: Int) {
        val items = uiState.value.checklistItems
        val completedItems = getCurrentCompletedItems()

        if (index < items.size) {
            if (index !in completedItems) {
                completedItems.add(index)
                // Move to next item if available
                findFirstUnchecked()?.let {
                    activeItemIndex.intValue = it
                }
            } else {
                // If the item is already completed, un-complete it
                completedItems.remove(index)
            }

            saveCurrentState()
        }
    }

    /**
     * Marks all items in the current list as complete
     */
    fun markAllItemsComplete() {
        val items = uiState.value.checklistItems
        val completedItems = getCurrentCompletedItems()

        // Add all indices to completed items if they aren't already there
        items.indices.forEach { index ->
            if (index !in completedItems) {
                completedItems.add(index)
            }
        }

        saveCurrentState()
    }

    /**
     * Gets the current completed items list
     */
    private fun getCurrentCompletedItems(): MutableList<Int> {
        return if (
            selectedSectionIndex.intValue < completedItemsBySection.size &&
            selectedListIndex.intValue < completedItemsBySection[selectedSectionIndex.intValue].size
        ) {
            completedItemsBySection[selectedSectionIndex.intValue][selectedListIndex.intValue]
        } else {
            mutableStateListOf()
        }
    }

    /**
     * Find the first unchecked item
     */
    private fun findFirstUnchecked(): Int? {
        val items = uiState.value.checklistItems
        val completedItems = uiState.value.completedItems
        return items.indices.firstOrNull { it !in completedItems }
    }

    /**
     * Skip to the next item
     */
    fun skipItem() {
        val items = uiState.value.checklistItems
        val completedItems = uiState.value.completedItems

        // Skip the current item and move to next without confirmation
        val nextUncheckedItem =
            items.indices.firstOrNull { it > activeItemIndex.intValue && it !in completedItems }
                ?: activeItemIndex.intValue
        activeItemIndex.intValue = nextUncheckedItem
    }

    /**
     * Find and navigate to skipped items
     */
    fun searchItem() {
        val items = uiState.value.checklistItems
        val completedItems = uiState.value.completedItems

        // Find the first skipped item (items that are not in completedItems)
        val firstSkipped = items.indices.firstOrNull {
            it !in completedItems && it != activeItemIndex.intValue
        }
        // If found, navigate to it
        firstSkipped?.let {
            activeItemIndex.intValue = it
        }
    }

    /**
     * Toggle microphone state
     */
    fun toggleMic() {
        isMicActive.value = !isMicActive.value
    }

    /**
     * Handle emergency action
     */
    fun handleEmergency() {
        // TODO: Implement emergency action functionality
    }

    /**
     * Toggle between tile grid view and list view
     */
    fun toggleTileGridView(showGrid: Boolean) {
        showingTileGrid.value = showGrid

        /** if (showGrid) {
            // Additional logic when showing grid
        }**/
    }

    /**
     * Data class that represents the UI state for the checklist screen
     */
    data class ChecklistUiState(
        val checklistTitle: String = "",
        val currentViewMode: String = "normalListView",
        val currentSectionType: String = "",
        val currentSectionLists: List<ChecklistList> = emptyList(),
        val checklistItems: List<ChecklistItem> = emptyList(),
        val completedItems: List<Int> = emptyList(),
        val hasMultipleSections: Boolean = false,
        val hasMultipleLists: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
