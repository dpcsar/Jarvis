package site.jarviscopilot.jarvis.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistItemData
import site.jarviscopilot.jarvis.data.model.ChecklistStateData
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import site.jarviscopilot.jarvis.util.TtsHandler

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
    val activeItemIndex: Int = -1,
    val checklistItemData: List<ChecklistItemData> = emptyList(),
    val completedItems: List<Int> = emptyList(),
    val isMicActive: Boolean = false,
    val showingTileGrid: Boolean = true,
    val blockedTasks: List<Int> = emptyList()
)

/**
 * ViewModel for the Checklist screen that handles all business logic and state management
 * following MVVM architecture principles
 */
class ChecklistViewModel(
    application: Application,
    private val repository: IChecklistRepository,
    private val stateManager: ChecklistStateManager,
    private val checklistName: String,
    resumeFromSaved: Boolean = false
) : AndroidViewModel(application) {

    // UI State
    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    // TTS Handler for speaking items
    private val ttsHandler = TtsHandler.getInstance(application)

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

        // Update checklist items (without TTS)
        updateCurrentChecklistItems()

        // Only proceed if we have valid data
        if (data != null && data.sections.isNotEmpty() && data.sections[0].lists.isNotEmpty()) {
            val items = data.sections[0].lists[0].listItems
            val section = data.sections[0]
            val list = section.lists[0]

            // Speak the initial checklist title, section title, list title, and items
            viewModelScope.launch {
                // Speak checklist title
                ttsHandler.handleChecklistOpened(data)

                // Speak section title
                ttsHandler.handleSectionOpened(section, TextToSpeech.QUEUE_ADD)

                // Speak list title if available
                ttsHandler.handleListOpened(
                    list.listTitle,
                    list.listTitleAudio,
                    TextToSpeech.QUEUE_ADD
                )

                // Process items until we find a task (this will speak the items)
                processItemsUntilTask(
                    items = items,
                    startIndex = 0,
                    queueMode = TextToSpeech.QUEUE_ADD
                )

                // Save state after updating
                saveCurrentState()
            }
        }
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

        // Find first task item to set as active only if activeItemIndex is not already set
        // or if it's out of bounds for the new item list
        val existingActiveIndex = currentState.activeItemIndex
        val useExistingIndex = existingActiveIndex >= 0 && existingActiveIndex < items.size

        var newActiveIndex = if (useExistingIndex) {
            existingActiveIndex  // Keep the existing active index if it's valid
        } else {
            // Otherwise find the first task item to set as active
            var firstTaskIndex = -1
            for (i in items.indices) {
                val item = items[i]
                // Check if the item is a task (not a label, note, caution, or warning)
                if (item.listItemType.equals("TASK", ignoreCase = true)) {
                    firstTaskIndex = i
                    break
                }
            }
            firstTaskIndex
        }

        // Calculate which tasks are blocked by previous required tasks
        val blockedTasks = calculateBlockedTasks(items, completedItemsList)

        // Determine if we should show tile grid based on the section's listView value
        val showTileGrid = currentSection.listView == "tileListView"

        _uiState.update {
            it.copy(
                currentViewMode = currentSection.listView,
                currentSectionType = currentSection.sectionType,
                currentSectionLists = currentSection.lists,  // Update the currentSectionLists property when switching sections
                hasMultipleLists = currentSection.lists.size > 1,
                hasMultipleSections = data.sections.size > 1,
                checklistItemData = items,              // Update the checklistItemData property
                completedItems = completedItemsList,    // Update the completedItems property
                activeItemIndex = newActiveIndex,       // Use the calculated active index (either preserved or new)
                blockedTasks = blockedTasks,            // Update blocked tasks
                showingTileGrid = showTileGrid         // Set showingTileGrid based on the section's listView property
            )
        }
    }

    /**
     * Calculate which tasks are blocked because previous required tasks are not completed
     * Returns a list of indices of tasks that cannot be checked
     */
    private fun calculateBlockedTasks(
        items: List<ChecklistItemData>,
        completedItems: List<Int>
    ): List<Int> {
        val blockedTasks = mutableListOf<Int>()
        var firstUncompletedRequiredTaskIndex = -1

        // Find the first uncompleted required task
        for (i in items.indices) {
            val item = items[i]
            val isComplete = i in completedItems

            // Only consider task items
            if (item.listItemType.equals("task", ignoreCase = true)) {
                // If we encounter an uncompleted required task
                if (item.isRequired && !isComplete) {
                    firstUncompletedRequiredTaskIndex = i
                    break
                }
            }
        }

        // If we found an uncompleted required task, block tasks that come after it
        if (firstUncompletedRequiredTaskIndex >= 0) {
            for (i in (firstUncompletedRequiredTaskIndex + 1) until items.size) {
                val item = items[i]
                val isComplete = i in completedItems

                // Only task items can be blocked
                if (item.listItemType.equals("task", ignoreCase = true) && !isComplete) {
                    blockedTasks.add(i)
                }
            }
        }

        return blockedTasks
    }

    /**
     * Restore saved state if available
     */
    private fun restoreSavedState() {
        val savedState = stateManager.getChecklistState(checklistName)
        if (savedState != null) {
            applyRestoredState(savedState)

            // After restoring the saved state, find and navigate to the first unchecked task
            // or the last item if all tasks are complete
            findFirstUncheckedTaskAcrossSections()
        }
    }

    /**
     * Apply a restored state to the UI
     */
    private fun applyRestoredState(state: ChecklistStateData) {
        val data = _uiState.value.checklistData ?: return

        // First, set the selected section and list indices
        _uiState.update { current ->
            current.copy(
                selectedSectionIndex = state.currentSectionIndex,
                selectedListIndex = state.currentListIndices[state.currentSectionIndex] ?: 0
            )
        }

        // Create a new completedItemsBySection structure
        val completedItemsBySection = mutableListOf<List<MutableList<Int>>>()

        // Initialize the structure with all sections and lists from the loaded data
        data.sections.forEachIndexed { sectionIdx, section ->
            val sectionLists = mutableListOf<MutableList<Int>>()
            section.lists.forEach { _ ->
                sectionLists.add(mutableListOf()) // Empty list for each list in the section
            }
            completedItemsBySection.add(sectionLists)
        }

        // Now apply all completed items from the saved state
        for ((compoundKey, itemIds) in state.completedItems) {
            // Parse the compound key to get section and list indices
            val keyParts = compoundKey.split("_")
            if (keyParts.size != 2) continue

            val sectionIndex = keyParts[0].toIntOrNull() ?: continue
            val listIndex = keyParts[1].toIntOrNull() ?: continue

            // Skip if out of bounds
            if (sectionIndex >= data.sections.size ||
                sectionIndex >= completedItemsBySection.size ||
                listIndex >= data.sections[sectionIndex].lists.size ||
                listIndex >= completedItemsBySection[sectionIndex].size
            ) {
                continue
            }

            // Get the list to update
            val completedList = completedItemsBySection[sectionIndex][listIndex]

            // For each item ID, find the corresponding index and add it to the completed list
            for (itemId in itemIds) {
                val index = findItemIndexById(itemId, sectionIndex, listIndex)
                if (index != -1) {
                    completedList.add(index)
                }
            }
        }

        // Update the UI state with the rebuilt completion data
        _uiState.update { current ->
            current.copy(completedItemsBySection = completedItemsBySection)
        }

        // Finally, update the current checklist items to show the right state
        updateCurrentChecklistItems()
    }

    /**
     * Find an item by its ID in the loaded checklist data
     * The itemId is now in the format "sectionIndex_listIndex_itemIndex"
     */
    private fun findItemIndexById(itemId: String, sectionIndex: Int, listIndex: Int): Int {
        // Parse the itemId to get the item index
        val parts = itemId.split("_")

        // If we have a position-based ID (should have 3 parts)
        if (parts.size == 3) {
            val storedSectionIndex = parts[0].toIntOrNull()
            val storedListIndex = parts[1].toIntOrNull()
            val storedItemIndex = parts[2].toIntOrNull()

            // If this ID is for the current section and list, and we have a valid item index
            if (storedSectionIndex == sectionIndex &&
                storedListIndex == listIndex &&
                storedItemIndex != null
            ) {
                return storedItemIndex
            }
        }

        // Fallback for older format IDs that might be using just the challenge
        // This helps with backward compatibility during transition
        val data = _uiState.value.checklistData ?: return -1
        if (sectionIndex >= data.sections.size) return -1

        val section = data.sections[sectionIndex]
        if (listIndex >= section.lists.size) return -1

        val list = section.lists[listIndex]
        // Try to find the item by its challenge as a fallback
        return list.listItems.indexOfFirst { it.challenge == itemId }
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

        // If attempting to check a task, verify it's not blocked by previous required tasks
        if (!wasComplete && itemIndex in currentState.blockedTasks) {
            // Task is blocked by a previous required task that's not completed
            return
        }

        if (wasComplete) {
            // Item is being unchecked
            listCompletedItems.remove(itemIndex)

            // Update the completed items lists
            sectionCompletedItems[listIndex] = listCompletedItems
            completedItems[sectionIndex] = sectionCompletedItems

            // Recalculate blocked tasks based on the new completion status
            val updatedBlockedTasks =
                calculateBlockedTasks(currentState.checklistItemData, listCompletedItems)

            // Update the state with the new completed items and blocked tasks
            _uiState.update {
                it.copy(
                    completedItemsBySection = completedItems,
                    completedItems = listCompletedItems,
                    blockedTasks = updatedBlockedTasks
                )
            }

            // Save state after updating
            saveCurrentState()

        } else {
            // Item is being marked as complete
            listCompletedItems.add(itemIndex)

            // Update the section's completed items list
            sectionCompletedItems[listIndex] = listCompletedItems
            completedItems[sectionIndex] = sectionCompletedItems

            // Recalculate blocked tasks based on the new completion status
            val updatedBlockedTasks =
                calculateBlockedTasks(currentState.checklistItemData, listCompletedItems)

            // First update the state with the new completed items and blocked tasks
            _uiState.update {
                it.copy(
                    completedItemsBySection = completedItems,
                    completedItems = listCompletedItems,
                    blockedTasks = updatedBlockedTasks // Update blocked tasks list
                )
            }

            // Process items until we find a task (this will speak the items)
            val items = _uiState.value.checklistItemData
            viewModelScope.launch {
                processItemsUntilTask(
                    items,
                    itemIndex + 1, // Start searching from the next item
                    TextToSpeech.QUEUE_FLUSH
                )

                // Check if we need to advance to next list or section
                // If there are no more unchecked tasks, we need to advance
                if (!items.drop(itemIndex + 1).any {
                        it.listItemType.equals("task", ignoreCase = true) &&
                                !_uiState.value.completedItems.contains(items.indexOf(it))
                    }) {
                    advanceToNextListOrSection()
                }

                // Save state after updating active item
                saveCurrentState()
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
            val newListIndex = currentListIdx + 1
            _uiState.update { it.copy(selectedListIndex = newListIndex) }
            saveCurrentState()
            updateCurrentChecklistItems()

            // Speak the next list title and first unchecked item
            val list = currentSection.lists.getOrNull(newListIndex)
            if (list != null) {
                viewModelScope.launch {
                    // Use TextToSpeech.QUEUE_ADD if useQueueAdd is true
                    ttsHandler.handleListOpened(
                        list.listTitle,
                        list.listTitleAudio,
                        TextToSpeech.QUEUE_FLUSH
                    )

                    // Process items until we find a task (this will speak the items)
                    val updatedState = _uiState.value
                    if (updatedState.checklistItemData.isNotEmpty()) {
                        processItemsUntilTask(
                            updatedState.checklistItemData,
                            0, // Start from beginning of the list
                            TextToSpeech.QUEUE_ADD
                        )

                        // Save the state with the updated active item
                        saveCurrentState()
                    }
                }
            }
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

                // Speak the section, list title, and first unchecked item
                viewModelScope.launch {
                    // Speak section name
                    ttsHandler.handleSectionOpened(nextSection)

                    // Speak list name if available
                    if (nextSection.lists.isNotEmpty()) {
                        val firstList = nextSection.lists.first()
                        ttsHandler.handleListOpened(
                            firstList.listTitle,
                            firstList.listTitleAudio,
                            TextToSpeech.QUEUE_ADD
                        )

                        // Process items until we find a task (this will speak the items)
                        val updatedState = _uiState.value
                        if (updatedState.checklistItemData.isNotEmpty()) {
                            processItemsUntilTask(
                                updatedState.checklistItemData,
                                0,
                                TextToSpeech.QUEUE_ADD
                            )

                            // Save the state with the updated active item
                            saveCurrentState()
                        }
                    }
                }
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

            // Speak the section title and list title when a new section is selected
            currentState.checklistData?.sections?.getOrNull(index)?.let { section ->
                viewModelScope.launch {
                    // Speak the section title
                    ttsHandler.handleSectionOpened(section)

                    // Only speak the first list title if not showing tile grid (not in tile view mode)
                    if (section.lists.isNotEmpty() && section.listView != "tileListView") {
                        val firstList = section.lists.first()
                        ttsHandler.handleListOpened(
                            firstList.listTitle, firstList.listTitleAudio,
                            TextToSpeech.QUEUE_FLUSH
                        )

                        // Process items until we find a task (this will speak the items)
                        val updatedState = _uiState.value
                        if (updatedState.checklistItemData.isNotEmpty()) {
                            processItemsUntilTask(
                                updatedState.checklistItemData,
                                0,
                                TextToSpeech.QUEUE_ADD
                            )

                            // Save the state with the updated active item
                            saveCurrentState()
                        }
                    }
                }
            }
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

        // Speak the list title when a new list is selected
        val currentState = _uiState.value
        val sectionIndex = currentState.selectedSectionIndex
        val section = currentState.checklistData?.sections?.getOrNull(sectionIndex)
        val list = section?.lists?.getOrNull(index)

        if (list != null) {
            viewModelScope.launch {
                ttsHandler.handleListOpened(list.listTitle, list.listTitleAudio)

                // Process items until we find a task (this will speak the items)
                // starting from the beginning of the list
                val updatedState = _uiState.value
                if (updatedState.checklistItemData.isNotEmpty()) {
                    processItemsUntilTask(
                        updatedState.checklistItemData,
                        0,
                        TextToSpeech.QUEUE_ADD
                    )

                    // Save the state with the updated active item
                    saveCurrentState()
                }
            }
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
                            currentState.checklistData?.sections?.get(sectionIdx)?.lists?.get(
                                listIdx
                            )?.listItems?.get(itemIdx)

                            // Use position-based unique identifier
                            val identifier = "${sectionIdx}_${listIdx}_${itemIdx}"

                            // Add the identifier to the list
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
        val checklistData = _uiState.value.checklistData ?: return
        val savedState = stateManager.getChecklistState(checklistName)
        val completedItems = savedState?.completedItems ?: mutableMapOf()

        // Store the current section and list indices to check if they change
        val currentSectionIndex = _uiState.value.selectedSectionIndex
        val currentListIndex = _uiState.value.selectedListIndex

        var foundItem = false  // Add a flag to track if we found an item

        // Start from the first section and search through all sections sequentially
        for (sectionIndex in checklistData.sections.indices) {
            val section = checklistData.sections[sectionIndex]

            // Skip non-checklist sections (like emergency or reference sections)
            if (section.sectionType != "checklist") continue

            val lists = section.lists

            // For each list in the section
            for (listIndex in lists.indices) {
                val list = lists[listIndex]
                val items = list.listItems

                // Get completed items for this section/list
                val compoundKey = "${sectionIndex}_${listIndex}"
                val completedInThisList = completedItems[compoundKey] ?: mutableListOf()

                // Find first unchecked task item
                for (itemIndex in items.indices) {
                    // Get the identifier for this item (position-based unique identifier)
                    val item = items[itemIndex]
                    val itemIdentifier = "${sectionIndex}_${listIndex}_${itemIndex}"

                    // Only process TASK type items, but continue checking the rest of the list
                    if (item.listItemType.equals("TASK", ignoreCase = true) &&
                        !completedInThisList.contains(itemIdentifier)
                    ) {
                        // Found an unchecked item - navigate to this section and list
                        foundItem = true  // Set flag that we found an item

                        // Track if section or list changed
                        val sectionChanged = sectionIndex != currentSectionIndex
                        val listChanged = listIndex != currentListIndex || sectionChanged

                        // First update the section if needed
                        if (sectionChanged) {
                            _uiState.update { it.copy(selectedSectionIndex = sectionIndex) }
                            // Force a full update of the section content
                            updateCurrentChecklistItems()
                        }

                        // Then update the list if needed
                        if (listChanged) {
                            _uiState.update { it.copy(selectedListIndex = listIndex) }
                            // Force a full update again to ensure list items are loaded
                            updateCurrentChecklistItems()
                        }

                        // Update UI state to set the active index
                        _uiState.update { it.copy(activeItemIndex = itemIndex) }

                        // Save the current state to persist the changes
                        saveCurrentState()

                        // Speak the item that was found, including any context
                        viewModelScope.launch {
                            // Only speak the section title if the section changed
                            if (sectionChanged) {
                                ttsHandler.handleSectionOpened(section)
                            }

                            // Only speak the list title if the list changed
                            if (listChanged) {
                                ttsHandler.handleListOpened(
                                    list.listTitle, list.listTitleAudio,
                                    TextToSpeech.QUEUE_ADD
                                )
                            }

                            // Then speak the item itself
                            ttsHandler.handleItem(
                                _uiState.value.checklistItemData,
                                itemIndex,
                                TextToSpeech.QUEUE_ADD
                            )
                        }

                        return  // Exit the function after finding and processing the first unchecked task
                    }
                    // Continue checking the rest of the items in the list
                }
                // If we've gone through the entire list and didn't find any incomplete tasks, continue to the next list
            }
        }

        // If we've gone through all sections and lists and didn't find any incomplete tasks
        @Suppress("SENSELESS_COMPARISON")
        if (!foundItem) {
            viewModelScope.launch {
                ttsHandler.handleMessage("No incomplete tasks found", TextToSpeech.QUEUE_FLUSH)
            }
        }
    }

    /**
     * Search for the first unchecked required item starting from the beginning section
     */
    fun searchRequiredItem() {
        val checklistData = _uiState.value.checklistData ?: return
        val savedState = stateManager.getChecklistState(checklistName)
        val completedItems = savedState?.completedItems ?: mutableMapOf()

        // Store the current section and list indices to check if they change
        val currentSectionIndex = _uiState.value.selectedSectionIndex
        val currentListIndex = _uiState.value.selectedListIndex

        var foundItem = false  // Add a flag to track if we found an item

        // Start from the first section and search through all sections sequentially
        for (sectionIndex in checklistData.sections.indices) {
            val section = checklistData.sections[sectionIndex]

            // Skip non-checklist sections (like emergency or reference sections)
            if (section.sectionType != "checklist") continue

            val lists = section.lists

            // For each list in the section
            for (listIndex in lists.indices) {
                val list = lists[listIndex]
                val items = list.listItems

                // Get completed items for this section/list
                val compoundKey = "${sectionIndex}_${listIndex}"
                val completedInThisList = completedItems[compoundKey] ?: mutableListOf()

                // Find first unchecked required item
                for (itemIndex in items.indices) {
                    val item = items[itemIndex]

                    // Get the identifier for this item (position-based unique identifier)
                    val itemIdentifier = "${sectionIndex}_${listIndex}_${itemIndex}"

                    if (item.isRequired &&
                        !completedInThisList.contains(itemIdentifier) &&
                        item.listItemType.equals("TASK", ignoreCase = true)
                    ) {
                        // Found an unchecked required item - navigate to this section and list
                        foundItem = true  // Set flag that we found an item

                        // Track if section or list changed
                        val sectionChanged = sectionIndex != currentSectionIndex
                        val listChanged = listIndex != currentListIndex || sectionChanged

                        // First update the section if needed
                        if (sectionChanged) {
                            _uiState.update { it.copy(selectedSectionIndex = sectionIndex) }
                            // Force a full update of the section content
                            updateCurrentChecklistItems()
                        }

                        // Then update the list if needed
                        if (listChanged) {
                            _uiState.update { it.copy(selectedListIndex = listIndex) }
                            // Force a full update again to ensure list items are loaded
                            updateCurrentChecklistItems()
                        }

                        // Update UI state to set the active index
                        _uiState.update { it.copy(activeItemIndex = itemIndex) }

                        // Save the current state to persist the changes
                        saveCurrentState()

                        // Speak the item that was found, including any context
                        viewModelScope.launch {
                            // Only speak the section title if the section changed
                            if (sectionChanged) {
                                ttsHandler.handleSectionOpened(section)
                            }

                            // Only speak the list title if the list changed
                            if (listChanged) {
                                ttsHandler.handleListOpened(
                                    list.listTitle, list.listTitleAudio,
                                    TextToSpeech.QUEUE_ADD
                                )
                            }

                            // Then speak the item itself with any preceding labels
                            ttsHandler.handleItem(
                                _uiState.value.checklistItemData,
                                itemIndex,
                                TextToSpeech.QUEUE_ADD
                            )
                        }

                        return
                    }
                }
            }
        }

        // If we've gone through all sections and lists and didn't find any incomplete required tasks
        @Suppress("SENSELESS_COMPARISON")
        if (!foundItem) {
            viewModelScope.launch {
                ttsHandler.handleMessage(
                    "No incomplete required tasks found",
                    TextToSpeech.QUEUE_FLUSH
                )
            }
        }
    }

    /**
     * Toggle microphone activation for voice commands
     */
    fun toggleMic() {
        _uiState.update { it.copy(isMicActive = !it.isMicActive) }
    }

    /**
     * Toggle between tile grid view and list view
     */
    fun toggleTileGridView(showGrid: Boolean) {
        // Set activeItemIndex to 0 before doing anything
        _uiState.update { it.copy(activeItemIndex = 0) }

        val currentState = _uiState.value
        val wasShowingGrid = currentState.showingTileGrid

        // Update UI state
        _uiState.update { it.copy(showingTileGrid = showGrid) }

        // If toggling from grid to list view, speak the list title and process items
        if (wasShowingGrid && !showGrid) {
            val sectionIndex = currentState.selectedSectionIndex
            val listIndex = currentState.selectedListIndex
            val section = currentState.checklistData?.sections?.getOrNull(sectionIndex)
            val list = section?.lists?.getOrNull(listIndex)

            if (list != null) {
                viewModelScope.launch {
                    // Speak the list title
                    ttsHandler.handleListOpened(list.listTitle, list.listTitleAudio)

                    // Process items until we find a task (this will speak the items)
                    val updatedState = _uiState.value
                    if (updatedState.checklistItemData.isNotEmpty()) {
                        processItemsUntilTask(
                            updatedState.checklistItemData,
                            0, // Start from beginning of the list
                            TextToSpeech.QUEUE_ADD
                        )

                        // Save the state with the updated active item
                        saveCurrentState()
                    }
                }
            }
        }
    }

    /**
     * Select a specific checklist item
     */
    fun selectChecklistItem(index: Int) {
        if (index >= 0 && index < _uiState.value.checklistItemData.size) {
            // Set the active item index
            val previousActiveIndex = _uiState.value.activeItemIndex
            _uiState.update { it.copy(activeItemIndex = index) }

            // Only continue if the active item has actually changed
            if (index != previousActiveIndex) {
                // Get the current section and list
                val currentState = _uiState.value
                val sectionIndex = currentState.selectedSectionIndex
                val listIndex = currentState.selectedListIndex
                val checklistData = currentState.checklistData

                if (checklistData != null &&
                    sectionIndex < checklistData.sections.size &&
                    listIndex < checklistData.sections[sectionIndex].lists.size
                ) {
                    // Speak the item that was selected, including any context
                    viewModelScope.launch {
                        // Speak the item itself using speakItemDirectly
                        ttsHandler.handleItem(
                            currentState.checklistItemData,
                            index,
                            TextToSpeech.QUEUE_ADD
                        )
                    }
                }
            }
        }
    }

    /**
     * Mark all items in current list as complete
     */
    fun markAllItemsComplete() {
        val currentState = _uiState.value
        val sectionIndex = currentState.selectedSectionIndex
        val listIndex = currentState.selectedListIndex

        // Get the current list's items
        val currentItems = currentState.checklistItemData
        if (currentItems.isEmpty()) return

        // Verify the indices are valid for the current state
        if (sectionIndex >= currentState.completedItemsBySection.size) return
        if (listIndex >= currentState.completedItemsBySection[sectionIndex].size) return

        // Create deep copies of the nested list structure to ensure we don't modify the existing state directly
        val completedItemsBySectionCopy = currentState.completedItemsBySection.map {
            it.map { list -> list.toMutableList() }.toMutableList()
        }.toMutableList()

        // Get the list we want to mark as complete
        val listToComplete = completedItemsBySectionCopy[sectionIndex][listIndex]

        // Clear the list and add all current indices
        listToComplete.clear()
        listToComplete.addAll(currentItems.indices)

        // Update the UI state with the new list
        _uiState.update { state ->
            state.copy(
                completedItemsBySection = completedItemsBySectionCopy,
                completedItems = listToComplete.toList() // Create a new list to ensure state update is detected
            )
        }

        // Save the state to persist changes between sessions
        saveCurrentState()

        // Speak the confirmation
        viewModelScope.launch {
            ttsHandler.handleMessage("All tasks marked complete", TextToSpeech.QUEUE_FLUSH)
        }

        // Advance to the next list or section
        advanceToNextListOrSection()
    }

    /**
     * Finds and selects the first emergency section in the checklist.
     * Used by the emergency button in the ChecklistBar.
     *
     * @return true if an emergency section was found and selected, false otherwise
     */
    fun selectFirstEmergencySection(): Boolean {
        val currentState = _uiState.value
        val data = currentState.checklistData ?: return false

        // Find the first emergency section
        val emergencySectionIndex = data.sections.indexOfFirst {
            it.sectionType == "emergency"
        }

        // If no emergency section found
        if (emergencySectionIndex == -1) return false

        // Select the emergency section
        _uiState.update {
            it.copy(
                selectedSectionIndex = emergencySectionIndex,
                selectedListIndex = 0,
                // Reset to showing tile grid if the emergency section is a tile list view
                showingTileGrid = data.sections[emergencySectionIndex].listView == "tileListView"
            )
        }

        saveCurrentState()
        updateCurrentChecklistItems()
        return true
    }

    /**
     * Find the first unchecked task across all sections and lists.
     * If all tasks are complete, returns the last item in the last section/list.
     * Updates the UI state to navigate to the appropriate section and list.
     */
    private fun findFirstUncheckedTaskAcrossSections() {
        val checklistData = _uiState.value.checklistData ?: return
        val savedState = stateManager.getChecklistState(checklistName)
        val completedItems = savedState?.completedItems ?: mutableMapOf()

        // Variables to track the last item position (fallback if all tasks are complete)
        var lastSectionIndex = 0
        var lastListIndex = 0
        var lastItemIndex = -1
        var foundLastItem = false

        // Start from the first section and search through all sections sequentially
        for (sectionIndex in checklistData.sections.indices) {
            val section = checklistData.sections[sectionIndex]
            val lists = section.lists

            // Skip non-checklist sections for initial task selection
            if (section.sectionType != "checklist") continue

            // For each list in the section
            for (listIndex in lists.indices) {
                val list = lists[listIndex]
                val items = list.listItems

                // Update last item position
                if (!items.isEmpty()) {
                    lastSectionIndex = sectionIndex
                    lastListIndex = listIndex
                    lastItemIndex = items.size - 1
                    foundLastItem = true
                }

                // Get completed items for this section/list
                val compoundKey = "${sectionIndex}_${listIndex}"
                val completedInThisList = completedItems[compoundKey] ?: mutableListOf()

                // Find first unchecked task item
                for (itemIndex in items.indices) {
                    // Get the identifier for this item (position-based unique identifier)
                    val item = items[itemIndex]
                    val itemIdentifier = "${sectionIndex}_${listIndex}_${itemIndex}"

                    // Check if this is a TASK and it's unchecked
                    if (!completedInThisList.contains(itemIdentifier) &&
                        item.listItemType.equals("TASK", ignoreCase = true)
                    ) {
                        // Found an unchecked item - navigate to this section and list
                        navigateToItem(sectionIndex, listIndex, itemIndex)
                        return
                    }
                }
            }
        }

        // If we get here, all tasks are complete or no tasks found
        // Fall back to the last item in the last section/list
        if (foundLastItem) {
            navigateToItem(lastSectionIndex, lastListIndex, lastItemIndex)
        }
    }

    /**
     * Helper method to navigate to a specific item by section, list and item indices
     */
    private fun navigateToItem(
        sectionIndex: Int,
        listIndex: Int,
        itemIndex: Int
    ) {
        // Change section and list indices and set the active item index
        _uiState.update {
            it.copy(
                selectedSectionIndex = sectionIndex,
                selectedListIndex = listIndex,
                activeItemIndex = itemIndex  // Explicitly set the active item index
            )
        }

        // Force a rebuild of the checklist items
        updateCurrentChecklistItems()

        // Save the current state to persist these changes
        saveCurrentState()

        // Explicitly speak the full context when navigating to an item (especially when resuming)
        val currentState = _uiState.value
        val data = currentState.checklistData ?: return

        if (sectionIndex < data.sections.size) {
            val section = data.sections[sectionIndex]
            val list = if (listIndex < section.lists.size) section.lists[listIndex] else null

            viewModelScope.launch {
                //  Speak section title
                ttsHandler.handleSectionOpened(section)

                // Speak list title if available
                if (list != null) {
                    ttsHandler.handleListOpened(
                        list.listTitle, list.listTitleAudio,
                        TextToSpeech.QUEUE_ADD
                    )
                }

                // Speak the item
                val items = currentState.checklistItemData
                if (items.isNotEmpty() && itemIndex != -1) {
                    ttsHandler.handleItem(
                        items, itemIndex,
                        TextToSpeech.QUEUE_ADD
                    )
                }
            }
        }
    }

    /**
     * Process items starting from the given index, reads them out loud,
     * and updates the UI state with the first unchecked task found
     *
     * @param items The list of checklist items
     * @param startIndex The starting index to begin reading from
     * @param queueMode TTS queue mode (QUEUE_FLUSH or QUEUE_ADD)
     */
    private suspend fun processItemsUntilTask(
        items: List<ChecklistItemData>,
        startIndex: Int,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        // Get the most up-to-date completed items list from the current UI state
        val currentCompletedItems = _uiState.value.completedItems

        // First check if all tasks in the checklist are complete
        val allTasksComplete = items.withIndex().all { (index, item) ->
            // If this is a task, verify it's in the completed items list, otherwise it's not a task so skip
            !item.listItemType.equals("task", ignoreCase = true) || currentCompletedItems.contains(
                index
            )
        }

        // If all tasks are complete, return early
        if (allTasksComplete) {
            return
        }

        // Start from the given index
        var currentIndex = startIndex

        // Continue reading items until we find a task
        while (currentIndex >= 0 && currentIndex < items.size) {
            val currentItem = items[currentIndex]

            // If this is a task
            if (currentItem.listItemType.equals("task", ignoreCase = true)) {
                // And it's not completed - use the current completed items instead of the parameter
                if (!currentCompletedItems.contains(currentIndex)) {
                    // Set this item as active in the UI
                    _uiState.update { it.copy(activeItemIndex = currentIndex) }
                    // Read the task and stop processing
                    ttsHandler.handleItem(items, currentIndex, queueMode)

                    return
                }
            } else {
                // If it's a non-task, read it and continue to the next item
                if (currentItem.challenge.isNotBlank()) {
                    // For non-tasks, we don't update the active item but still speak it
                    ttsHandler.handleItem(items, currentIndex, queueMode)
                }
            }

            // Move to the next item
            currentIndex++
        }
        // No unchecked task was found, we've reached the end of the list
    }
}
