package site.jarviscopilot.jarvis.viewmodel

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
    resumeFromSaved: Boolean = false
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

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

        // Initialize completedItemsBySection
        val completedItems = data?.sections?.map { section ->
            section.lists.map { mutableListOf<Int>() }
        } ?: listOf(listOf(mutableListOf()))

        // Update UI state with loaded data
        _uiState.update { currentState ->
            currentState.copy(
                checklistData = data,
                checklistTitle = data?.title ?: "",
                completedItemsBySection = completedItems,
                // Initialize other state properties based on the data
                currentSectionLists = if (data != null && data.sections.isNotEmpty())
                    data.sections[0].lists else emptyList(),
                currentViewMode = if (data != null && data.sections.isNotEmpty())
                    data.sections[0].listView else "normalListView",
                currentSectionType = if (data != null && data.sections.isNotEmpty())
                    data.sections[0].sectionType else "",
                hasMultipleSections = (data?.sections?.size ?: 0) > 1,
                hasMultipleLists = (data?.sections?.firstOrNull()?.lists?.size ?: 0) > 1,
                isLoading = false
            )
        }

        updateCurrentChecklistItems()
    }

    /**
     * Updates the current checklist items based on selected section and list
     */
    private fun updateCurrentChecklistItems() {
        val currentState = _uiState.value
        val data = currentState.checklistData ?: return

        if (currentState.selectedSectionIndex >= data.sections.size) {
            _uiState.update { it.copy(selectedSectionIndex = 0) }
        }

        val sectionIndex = currentState.selectedSectionIndex
        val currentSection = data.sections[sectionIndex]

        if (currentState.selectedListIndex >= currentSection.lists.size) {
            _uiState.update { it.copy(selectedListIndex = 0) }
        }

        val listIndex = currentState.selectedListIndex
        val currentList = if (listIndex < currentSection.lists.size) {
            currentSection.lists[listIndex]
        } else null

        val items = currentList?.listItems ?: emptyList()

        val currentCompletedItems = if (
            sectionIndex < currentState.completedItemsBySection.size &&
            listIndex < currentState.completedItemsBySection[sectionIndex].size
        ) {
            currentState.completedItemsBySection[sectionIndex][listIndex]
        } else {
            emptyList()
        }

        _uiState.update {
            it.copy(
                currentViewMode = currentSection.listView,
                currentSectionType = currentSection.sectionType,
                currentSectionLists = currentSection.lists,
                checklistItems = items,
                completedItems = currentCompletedItems,
                hasMultipleLists = currentSection.lists.size > 1
            )
        }
    }

    /**
     * Restores saved state if available
     */
    private fun restoreSavedState() {
        val savedState = stateManager.getChecklistState(checklistName) ?: return

        // Update UI state with saved state
        _uiState.update { currentState ->
            currentState.copy(
                selectedSectionIndex = savedState.sectionIndex,
                selectedListIndex = savedState.listIndex,
                activeItemIndex = savedState.activeItemIndex
            )
        }

        // Restore completed items
        try {
            val currentState = _uiState.value
            val updatedCompletedItems = currentState.completedItemsBySection.toMutableList()

            savedState.completedItems.forEachIndexed { sectionIdx, sectionLists ->
                if (sectionIdx < updatedCompletedItems.size) {
                    val sectionItems = updatedCompletedItems[sectionIdx].toMutableList()

                    sectionLists.forEachIndexed { listIdx, completedIndices ->
                        if (listIdx < sectionItems.size) {
                            sectionItems[listIdx] = completedIndices.toMutableList()
                        }
                    }

                    updatedCompletedItems[sectionIdx] = sectionItems
                }
            }

            _uiState.update {
                it.copy(completedItemsBySection = updatedCompletedItems)
            }

        } catch (_: Exception) {
            // Handle potential index out of bounds if checklist structure changed
        }

        updateCurrentChecklistItems()
    }

    /**
     * Saves the current state
     */
    fun saveCurrentState() {
        val currentState = _uiState.value

        // Convert completedItemsBySection to a regular List for serialization
        val serializedCompletedItems = currentState.completedItemsBySection

        val state = ChecklistState(
            checklistFilename = checklistName,
            sectionIndex = currentState.selectedSectionIndex,
            listIndex = currentState.selectedListIndex,
            activeItemIndex = currentState.activeItemIndex,
            completedItems = serializedCompletedItems
        )

        stateManager.saveChecklistState(state)
    }

    /**
     * Handle selection of a section
     */
    fun selectSection(index: Int) {
        val currentState = _uiState.value

        if (index == currentState.selectedSectionIndex) return

        val data = currentState.checklistData ?: return
        if (index >= 0 && index < data.sections.size) {
            val newSectionType = data.sections[index].sectionType.lowercase()
            val shouldShowTileGrid = newSectionType == "emergency" || newSectionType == "reference"

            _uiState.update {
                it.copy(
                    selectedSectionIndex = index,
                    selectedListIndex = 0,
                    activeItemIndex = 0,
                    showingTileGrid = shouldShowTileGrid
                )
            }

            updateCurrentChecklistItems()
        }
    }

    /**
     * Handle selection of a list
     */
    fun selectList(index: Int) {
        val currentState = _uiState.value

        if (index == currentState.selectedListIndex) return

        val data = currentState.checklistData ?: return
        val sectionIndex = currentState.selectedSectionIndex

        if (sectionIndex < data.sections.size) {
            val section = data.sections[sectionIndex]
            if (index >= 0 && index < section.lists.size) {
                _uiState.update {
                    it.copy(
                        selectedListIndex = index,
                        activeItemIndex = 0
                    )
                }

                updateCurrentChecklistItems()
            }
        }
    }

    /**
     * Select a specific checklist item
     */
    fun selectChecklistItem(index: Int) {
        val currentState = _uiState.value
        val items = currentState.checklistItems

        if (index >= 0 && index < items.size) {
            _uiState.update { it.copy(activeItemIndex = index) }
        }
    }

    /**
     * Toggle completion status of a checklist item
     */
    fun toggleCompleteItem(index: Int) {
        val currentState = _uiState.value
        val items = currentState.checklistItems

        if (index < items.size) {
            val completedItems = currentState.completedItems.toMutableList()

            if (index !in completedItems) {
                completedItems.add(index)

                // Update the completedItemsBySection
                updateCompletedItems(completedItems)

                // Move to next item if available
                findFirstUnchecked()?.let { nextIndex ->
                    _uiState.update { it.copy(activeItemIndex = nextIndex) }
                }
            } else {
                // If the item is already completed, un-complete it
                completedItems.remove(index)

                // Update the completedItemsBySection
                updateCompletedItems(completedItems)
            }

            saveCurrentState()
        }
    }

    /**
     * Updates the completedItems in the completedItemsBySection
     */
    private fun updateCompletedItems(updatedCompletedItems: List<Int>) {
        val currentState = _uiState.value
        val sectionIndex = currentState.selectedSectionIndex
        val listIndex = currentState.selectedListIndex

        val updatedSections =
            currentState.completedItemsBySection.mapIndexed { secIdx, sectionLists ->
                if (secIdx == sectionIndex) {
                    sectionLists.mapIndexed { lstIdx, listItems ->
                        if (lstIdx == listIndex) updatedCompletedItems.toMutableList() else listItems
                    }
                } else {
                    sectionLists
                }
            }

        _uiState.update {
            it.copy(
                completedItemsBySection = updatedSections,
                completedItems = updatedCompletedItems
            )
        }
    }

    /**
     * Find the first unchecked item
     */
    private fun findFirstUnchecked(): Int? {
        val currentState = _uiState.value
        val items = currentState.checklistItems
        val completedItems = currentState.completedItems

        return items.indices.firstOrNull { it !in completedItems }
    }

    /**
     * Marks all items in the current list as complete
     */
    fun markAllItemsComplete() {
        val currentState = _uiState.value
        val items = currentState.checklistItems

        // Create a list with all indices included
        val allCompleted = items.indices.toList()

        // Update the completedItemsBySection
        updateCompletedItems(allCompleted)

        saveCurrentState()
    }

    /**
     * Skip to the next item
     */
    fun skipItem() {
        val currentState = _uiState.value
        val items = currentState.checklistItems
        val completedItems = currentState.completedItems
        val activeItemIndex = currentState.activeItemIndex

        // Skip the current item and move to next without confirmation
        val nextUncheckedItem =
            items.indices.firstOrNull { it > activeItemIndex && it !in completedItems }
                ?: activeItemIndex

        _uiState.update { it.copy(activeItemIndex = nextUncheckedItem) }
    }

    /**
     * Find and navigate to skipped items
     */
    fun searchItem() {
        val currentState = _uiState.value
        val items = currentState.checklistItems
        val completedItems = currentState.completedItems
        val activeItemIndex = currentState.activeItemIndex

        // Find the first skipped item (items that are not in completedItems)
        val firstSkipped = items.indices.firstOrNull {
            it !in completedItems && it != activeItemIndex
        }

        // If found, navigate to it
        firstSkipped?.let { skippedIndex ->
            _uiState.update { it.copy(activeItemIndex = skippedIndex) }
        }
    }

    /**
     * Toggle microphone state
     */
    fun toggleMic() {
        val currentState = _uiState.value
        _uiState.update { it.copy(isMicActive = !currentState.isMicActive) }
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
        _uiState.update { it.copy(showingTileGrid = showGrid) }
    }

    /**
     * Determines if the current list is completely checked off
    fun isCurrentListComplete(): Boolean {
    val currentState = _uiState.value
    val items = currentState.checklistItems
    val completedItems = currentState.completedItems

    return ChecklistUtils.isChecklistComplete(completedItems, items.size)
    }
     */

    /**
     * Data class that represents the UI state for the checklist screen
     */
    data class ChecklistUiState(
        val checklistData: ChecklistData? = null,
        val checklistTitle: String = "",
        val currentViewMode: String = "normalListView",
        val currentSectionType: String = "",
        val currentSectionLists: List<ChecklistList> = emptyList(),
        val checklistItems: List<ChecklistItem> = emptyList(),
        val completedItems: List<Int> = emptyList(),
        val completedItemsBySection: List<List<MutableList<Int>>> = emptyList(),
        val selectedSectionIndex: Int = 0,
        val selectedListIndex: Int = 0,
        val activeItemIndex: Int = 0,
        val hasMultipleSections: Boolean = false,
        val hasMultipleLists: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isMicActive: Boolean = false,
        val showingTileGrid: Boolean = false
    )
}
