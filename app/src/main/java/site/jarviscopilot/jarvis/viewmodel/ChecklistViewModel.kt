package site.jarviscopilot.jarvis.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistItemData
import site.jarviscopilot.jarvis.data.model.ChecklistStateData
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager

/**
 * UI State class for the Checklist screen
 */
data class ChecklistUiState(
    val isLoading: Boolean = true,
    val checklistData: ChecklistData? = null,
    val checklistTitle: String = "",
    val selectedSectionIndex: Int = 0,
    val selectedListIndex: Int = 0,
    val currentSectionType: String = "",
    val currentSectionLists: List<site.jarviscopilot.jarvis.data.model.ChecklistListData> = emptyList(),
    val completedItemsBySection: List<List<MutableList<Int>>> = emptyList(),
    val currentViewMode: String = "normalListView",
    val hasMultipleSections: Boolean = false,
    val hasMultipleLists: Boolean = false,
    val shouldShowCompletionDialog: Boolean = false,
    val activeItemIndex: Int = -1,
    val checklistItemData: List<ChecklistItemData> = emptyList(),
    val completedItems: List<Int> = emptyList(),
    val isMicActive: Boolean = false,
    val showingTileGrid: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel for the Checklist screen that handles all business logic and state management
 * following MVVM architecture principles
 */
class ChecklistViewModel(
    private val repository: IChecklistRepository,
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

        // Get the completed items for the current section and list
        val completedItemsList = if (sectionIndex < currentState.completedItemsBySection.size &&
            listIndex < currentState.completedItemsBySection[sectionIndex].size
        ) {
            currentState.completedItemsBySection[sectionIndex][listIndex]
        } else {
            emptyList()
        }

        // Find first task item to set as active
        var firstTaskIndex = -1
        for (i in items.indices) {
            val item = items[i]
            // Check if the item is a task (not a label, note, caution, or warning)
            if (item.listItemType.equals("TASK", ignoreCase = true)) {
                firstTaskIndex = i
                break
            }
        }

        _uiState.update {
            it.copy(
                currentViewMode = currentSection.listView,
                currentSectionType = currentSection.sectionType,
                currentSectionLists = currentSection.lists,  // Update the currentSectionLists property when switching sections
                hasMultipleLists = currentSection.lists.size > 1,
                hasMultipleSections = data.sections.size > 1,
                checklistItemData = items,              // Update the checklistItemData property
                completedItems = completedItemsList,    // Update the completedItems property
                // Set the active item to the first task item, not just the first item in the list
                activeItemIndex = firstTaskIndex
            )
        }

        // Check if all items are completed to show completion dialog
        checkCompletion()
    }

    /**
     * Restore saved state if available
     */
    private fun restoreSavedState() {
        val savedState = stateManager.getChecklistState(checklistName)
        if (savedState != null) {
            applyRestoredState(savedState)
        }
    }

    /**
     * Apply a restored state to the UI
     */
    private fun applyRestoredState(state: ChecklistStateData) {
        _uiState.update { current ->
            current.copy(
                selectedSectionIndex = state.currentSectionIndex,
                selectedListIndex = state.currentListIndices[state.currentSectionIndex] ?: 0
            )
        }

        // Apply completed items
        for ((compoundKey, itemIds) in state.completedItems) {
            // Parse the compound key to get section and list indices
            val keyParts = compoundKey.split("_")
            if (keyParts.size != 2) continue

            val sectionIndex = keyParts[0].toIntOrNull() ?: continue
            val listIndex = keyParts[1].toIntOrNull() ?: continue

            itemIds.forEach { itemId ->
                val index = findItemIndexById(itemId, sectionIndex, listIndex)
                if (index != -1) {
                    toggleItemCompletion(index)
                }
            }
        }

        updateCurrentChecklistItems()
    }

    /**
     * Find an item by its ID in the loaded checklist data
     */
    private fun findItemIndexById(itemId: String, sectionIndex: Int, listIndex: Int): Int {
        val data = _uiState.value.checklistData ?: return -1
        if (sectionIndex >= data.sections.size) return -1

        val section = data.sections[sectionIndex]
        if (listIndex >= section.lists.size) return -1

        val list = section.lists[listIndex]
        // Use challenge as the identifier since itemId property doesn't exist in ChecklistItem
        return list.listItems.indexOfFirst { it.challenge == itemId }
    }

    /**
     * Check if the item is the last task in the list
     * @return true if this is the last task item in the sequence, false otherwise
     */
    private fun isLastTaskInSequence(itemIndex: Int): Boolean {
        val currentState = _uiState.value
        val items = currentState.checklistItemData

        // Check if there are any more task items after this one
        for (i in (itemIndex + 1) until items.size) {
            val item = items[i]
            if (item.listItemType.equals("TASK", ignoreCase = true)) {
                return false // Found another task after this one
            }
        }

        // If we didn't find any more task items, this is the last one
        return true
    }

    /**
     * Toggle completion state of a checklist item
     */
    fun toggleItemCompletion(itemIndex: Int) {
        val currentState = _uiState.value
        val sectionIndex = currentState.selectedSectionIndex
        val listIndex = currentState.selectedListIndex

        if (sectionIndex >= currentState.completedItemsBySection.size) return
        if (listIndex >= currentState.completedItemsBySection[sectionIndex].size) return

        val completedItems = currentState.completedItemsBySection.toMutableList()
        val sectionCompletedItems = completedItems[sectionIndex].toMutableList()
        val listCompletedItems = sectionCompletedItems[listIndex].toMutableList()

        val wasComplete = itemIndex in listCompletedItems

        // Store information about whether this is the last task in the sequence
        val isLastTask = !wasComplete &&
                currentState.checklistItemData.getOrNull(itemIndex)?.listItemType?.equals(
                    "TASK",
                    ignoreCase = true
                ) == true &&
                isLastTaskInSequence(itemIndex)

        if (wasComplete) {
            listCompletedItems.remove(itemIndex)

            // Update the completed items lists
            sectionCompletedItems[listIndex] = listCompletedItems
            completedItems[sectionIndex] = sectionCompletedItems

            // Update the state with the new completed items
            _uiState.update {
                it.copy(
                    completedItemsBySection = completedItems,
                    completedItems = listCompletedItems,
                    activeItemIndex = itemIndex // Set unchecked item as active
                )
            }

            // Save state after updating
            saveCurrentState()

        } else {
            listCompletedItems.add(itemIndex)

            // Update the section's completed items list
            sectionCompletedItems[listIndex] = listCompletedItems
            completedItems[sectionIndex] = sectionCompletedItems

            // First update the state with the new completed items
            _uiState.update {
                it.copy(
                    completedItemsBySection = completedItems,
                    completedItems = listCompletedItems
                )
            }

            // Then find the next unchecked item (now that our state is updated)
            val nextUncheckedItem = findNextUncheckedItem()

            // Update the active item if we found a valid next unchecked item
            if (nextUncheckedItem != -1) {
                _uiState.update {
                    it.copy(activeItemIndex = nextUncheckedItem)
                }

                // Save state and check completion after updating active item
                saveCurrentState()
                checkCompletion()
            } else {
                // No more unchecked items in this list
                // Save state and check completion
                saveCurrentState()
                checkCompletion()

                // Always advance to the next list/section when we've completed all items in current list
                advanceToNextListOrSection()
            }
            return
        }
    }

    /**
     * Advances to the next appropriate list or section after completing all items in the current list.
     * Only advances if current section type is "checklist" and doesn't advance to emergency or reference sections.
     */
    private fun advanceToNextListOrSection() {
        val currentState = _uiState.value
        val data = currentState.checklistData ?: return

        val currentSectionIdx = currentState.selectedSectionIndex
        val currentListIdx = currentState.selectedListIndex
        val currentSection = data.sections.getOrNull(currentSectionIdx) ?: return

        // Only proceed with auto-advancement if we're in a checklist section
        if (currentSection.sectionType != "checklist") return

        // First try to advance to the next list in the current section
        if (currentListIdx < currentSection.lists.size - 1) {
            // There's another list in this section, go to it
            _uiState.update { it.copy(selectedListIndex = currentListIdx + 1) }
            saveCurrentState()
            updateCurrentChecklistItems()
            return
        }

        // If we're at the last list in the section, try to find the next checklist section
        var nextSectionIdx = currentSectionIdx + 1
        while (nextSectionIdx < data.sections.size) {
            val nextSection = data.sections[nextSectionIdx]
            // Only advance to another checklist section, skip emergency and reference sections
            if (nextSection.sectionType == "checklist") {
                _uiState.update {
                    it.copy(
                        selectedSectionIndex = nextSectionIdx,
                        selectedListIndex = 0,
                        // Reset to showing tile grid if the next section is a tile list view
                        showingTileGrid = nextSection.listView == "tileListView"
                    )
                }
                saveCurrentState()
                updateCurrentChecklistItems()
                return
            }
            nextSectionIdx++
        }

        // If we get here, there are no more checklist sections to advance to
    }

    /**
     * Select a specific section
     */
    fun selectSection(index: Int) {
        val currentState = _uiState.value
        val currentSectionType =
            currentState.checklistData?.sections?.getOrNull(currentState.selectedSectionIndex)?.listView

        // If clicking on the same section that uses tileListView and currently not showing tile grid,
        // toggle back to tile grid view
        if (index == currentState.selectedSectionIndex &&
            currentSectionType == "tileListView" &&
            !currentState.showingTileGrid
        ) {

            _uiState.update { it.copy(showingTileGrid = true) }
            return
        }

        // If switching to a new section
        if (index != currentState.selectedSectionIndex) {
            val newSectionType =
                currentState.checklistData?.sections?.getOrNull(index)?.listView ?: "normalListView"

            // Reset to showing the tile grid when switching to a section with tileListView
            val resetTileGrid = newSectionType == "tileListView"

            _uiState.update {
                it.copy(
                    selectedSectionIndex = index,
                    selectedListIndex = 0,
                    showingTileGrid = resetTileGrid  // Reset to showing tile grid when switching sections
                )
            }

            saveCurrentState()
            updateCurrentChecklistItems()
        }
    }

    /**
     * Select a specific list
     */
    fun selectList(index: Int) {
        if (index == _uiState.value.selectedListIndex) return

        _uiState.update { it.copy(selectedListIndex = index) }

        saveCurrentState()
        updateCurrentChecklistItems()
    }

    /**
     * Check if the current checklist is completed
     */
    private fun checkCompletion() {
        val currentState = _uiState.value
        val data = currentState.checklistData ?: return

        var allCompleted = true

        for (sectionIdx in data.sections.indices) {
            val section = data.sections[sectionIdx]

            for (listIdx in section.lists.indices) {
                val list = section.lists[listIdx]

                // Get required items
                val requiredItems = list.listItems
                    .mapIndexedNotNull { idx, item -> if (item.isRequired) idx else null }

                // Check if all required items are completed
                if (sectionIdx < currentState.completedItemsBySection.size &&
                    listIdx < currentState.completedItemsBySection[sectionIdx].size
                ) {

                    val completedItems = currentState.completedItemsBySection[sectionIdx][listIdx]

                    if (!requiredItems.all { it in completedItems }) {
                        allCompleted = false
                        break
                    }
                } else if (requiredItems.isNotEmpty()) {
                    allCompleted = false
                    break
                }
            }

            if (!allCompleted) break
        }

        if (allCompleted && currentState.shouldShowCompletionDialog == false) {
            _uiState.update { it.copy(shouldShowCompletionDialog = true) }
        }
    }

    /**
     * Save the current state of the checklist
     */
    fun saveCurrentState() {
        val currentState = _uiState.value

        // Convert to map structure for serialization
        val completedItems = mutableMapOf<String, MutableList<String>>()

        currentState.completedItemsBySection.forEachIndexed { sectionIdx, lists ->
            lists.forEachIndexed { listIdx, items ->
                if (items.isNotEmpty()) {
                    // Create a compound key that combines section and list indices
                    val compoundKey = "${sectionIdx}_${listIdx}"
                    val itemIds = mutableListOf<String>()

                    items.forEach { itemIdx ->
                        if (itemIdx >= 0 &&
                            sectionIdx < (currentState.checklistData?.sections?.size ?: 0) &&
                            listIdx < (currentState.checklistData?.sections?.get(sectionIdx)?.lists?.size
                                ?: 0) &&
                            itemIdx < (currentState.checklistData?.sections?.get(sectionIdx)?.lists?.get(
                                listIdx
                            )?.listItems?.size ?: 0)
                        ) {
                            // Get the item
                            val item =
                                currentState.checklistData?.sections?.get(sectionIdx)?.lists?.get(
                                    listIdx
                                )?.listItems?.get(itemIdx)

                            // Use challenge as the identifier (or fallback to index)
                            val identifier = if (!item?.challenge.isNullOrEmpty()) {
                                item.challenge
                            } else {
                                itemIdx.toString() // Fallback to using the index as a string
                            }

                            // Add the identifier to the list (no need to check if null)
                            itemIds.add(identifier)
                        }
                    }

                    if (itemIds.isNotEmpty()) {
                        completedItems[compoundKey] = itemIds
                    }
                }
            }
        }

        val currentListIndices = mutableMapOf<Int, Int>()
        currentListIndices[currentState.selectedSectionIndex] = currentState.selectedListIndex

        val checklistStateData = ChecklistStateData(
            checklistName = checklistName,
            currentSectionIndex = currentState.selectedSectionIndex,
            currentListIndices = currentListIndices,
            completedItems = completedItems
        )

        repository.saveChecklistState(checklistStateData)
    }

    /**
     * Skip the current active item and move to the next one
     */
    fun skipItem() {
        val currentState = _uiState.value
        val nextIndex = currentState.activeItemIndex + 1

        // Only proceed if there are more items to skip to
        if (nextIndex < currentState.checklistItemData.size) {
            _uiState.update { it.copy(activeItemIndex = nextIndex) }
        }
    }

    /**
     * Handle search functionality for checklist items
     */
    fun searchItem() {
        // Placeholder for search functionality
        // This would typically show a search UI or filter items based on search criteria
    }

    /**
     * Toggle microphone activation for voice commands
     */
    fun toggleMic() {
        _uiState.update { it.copy(isMicActive = !it.isMicActive) }
    }

    /**
     * Handle emergency procedures
     */
    fun handleEmergency() {
        // Placeholder for emergency handling functionality
        // This might navigate to emergency procedures or highlight critical checklist items
    }

    /**
     * Toggle between tile grid view and list view
     */
    fun toggleTileGridView(showGrid: Boolean) {
        _uiState.update { it.copy(showingTileGrid = showGrid) }
    }

    /**
     * Select a specific checklist item
     */
    fun selectChecklistItem(index: Int) {
        if (index >= 0 && index < _uiState.value.checklistItemData.size) {
            _uiState.update { it.copy(activeItemIndex = index) }
        }
    }

    /**
     * Mark all items in current list as complete
     */
    fun markAllItemsComplete() {
        val currentState = _uiState.value
        val sectionIndex = currentState.selectedSectionIndex
        val listIndex = currentState.selectedListIndex

        if (sectionIndex >= currentState.completedItemsBySection.size) return
        if (listIndex >= currentState.completedItemsBySection[sectionIndex].size) return

        val completedItems = currentState.completedItemsBySection.toMutableList()
        val sectionCompletedItems = completedItems[sectionIndex].toMutableList()
        val listCompletedItems = sectionCompletedItems[listIndex].toMutableList()

        // Add all item indices to the completed items list
        val allIndices = currentState.checklistItemData.indices.toList()
        listCompletedItems.clear()
        listCompletedItems.addAll(allIndices)

        sectionCompletedItems[listIndex] = listCompletedItems
        completedItems[sectionIndex] = sectionCompletedItems

        // Create a new list to ensure Compose detects the state change
        val newCompletedItemsList = listCompletedItems.toList()

        _uiState.update {
            it.copy(
                completedItemsBySection = completedItems,
                // Update the completedItems list as well to trigger UI refresh
                completedItems = newCompletedItemsList
            )
        }

        // Save state after marking all complete
        saveCurrentState()

        // Check completion
        checkCompletion()

        // Advance to next list/section after marking all items complete
        advanceToNextListOrSection()
    }

    /**
     * Find the next unchecked item in the current list
     * Returns -1 if no unchecked items are found
     */
    private fun findNextUncheckedItem(): Int {
        val currentState = _uiState.value
        val completedItems = currentState.completedItems
        val activeIndex = currentState.activeItemIndex

        // Count total remaining unchecked task items
        var remainingUncheckedTasks = 0
        for (i in currentState.checklistItemData.indices) {
            val item = currentState.checklistItemData.getOrNull(i)
            if (item != null && !completedItems.contains(i) &&
                item.listItemType.equals("TASK", ignoreCase = true)) {
                remainingUncheckedTasks++
            }
        }

        // If there are no unchecked tasks left, return -1
        if (remainingUncheckedTasks == 0) {
            return -1
        }

        // Start searching from the item after the active item
        val startIndex = activeIndex + 1

        // First try to find an unchecked item after the current active item
        for (i in startIndex until currentState.checklistItemData.size) {
            // Check if this item is a TASK item that can be checked
            val item = currentState.checklistItemData.getOrNull(i)
            if (item != null && !completedItems.contains(i) &&
                item.listItemType.equals("TASK", ignoreCase = true)) {
                return i
            }
        }

        // No unchecked items found after the active item
        // If we started from a non-beginning index, return -1 to trigger moving to next list/section
        // This ensures we don't loop back to the beginning of the current list
        return -1
    }
}
