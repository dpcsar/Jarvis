package site.jarviscopilot.jarvis.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.R
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.model.Checklist
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.model.ChecklistSection
import site.jarviscopilot.jarvis.util.Constants
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class ChecklistUiState(
    val isLoading: Boolean = true,
    val checklist: Checklist? = null,
    val error: String? = null,
    val selectedSectionIndex: Int = 0,
    val selectedListIndex: Int = 0,
    val selectedItemIndex: Int = 0,
    val currentLocalTime: String = "",
    val currentUtcTime: String = ""
)

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ChecklistRepository(application)
    
    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()
    
    init {
        loadChecklist()
        startTimeUpdates()
        Log.d(Constants.LOG_TAG_CHECKLIST_VIEW_MODEL, "ViewModel initialized")
    }
    
    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                updateTime()
                delay(1000) // More efficient than creating a flow with emissions
            }
        }
    }
    
    private fun updateTime() {
        val now = LocalDateTime.now()
        val utcNow = LocalDateTime.now(ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern(Constants.TIME_FORMAT_PATTERN)

        _uiState.update { currentState ->
            currentState.copy(
                currentLocalTime = formatter.format(now),
                currentUtcTime = formatter.format(utcNow)
            )
        }
    }
    
    private fun loadChecklist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.loadChecklistFromAssets()
                .onSuccess { checklist ->
                    Log.d(
                        Constants.LOG_TAG_CHECKLIST_VIEW_MODEL,
                        getApplication<Application>().resources.getQuantityString(
                            R.plurals.log_checklist_loaded,
                            checklist.sections.size,
                            checklist.name,
                            checklist.sections.size
                        )
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            checklist = checklist,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e(Constants.LOG_TAG_CHECKLIST_VIEW_MODEL, "Failed to load checklist", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = getApplication<Application>().getString(
                                R.string.error_load_checklist,
                                exception.message ?: ""
                            )
                        )
                    }
                }
        }
    }
    
    fun selectSection(index: Int) {
        val checklist = _uiState.value.checklist ?: return
        
        if (index < 0 || index >= checklist.sections.size) {
            return
        }
        
        val section = checklist.sections[index]

        // Default values in case we don't find anything
        var targetListIndex = 0
        var targetItemIndex = 0

        // Only perform special navigation for checklist type sections
        if (section.type == Constants.SECTION_TYPE_CHECKLIST && section.lists.isNotEmpty()) {
            var foundUncheckedItem = false

            // First pass: look for the first unchecked item
            for (listIdx in section.lists.indices) {
                val list = section.lists[listIdx]
                for (itemIdx in list.items.indices) {
                    val item = list.items[itemIdx]
                    if (!item.checked) {
                        targetListIndex = listIdx
                        targetItemIndex = itemIdx
                        foundUncheckedItem = true
                        break
                    }
                }
                if (foundUncheckedItem) break
            }

            // If all items are checked, navigate to the last item in the last list
            if (!foundUncheckedItem && section.lists.isNotEmpty()) {
                val lastList = section.lists.last()
                if (lastList.items.isNotEmpty()) {
                    targetListIndex = section.lists.size - 1
                    targetItemIndex = lastList.items.size - 1
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedSectionIndex = index,
                selectedListIndex = targetListIndex,
                selectedItemIndex = targetItemIndex
            )
        }
    }
    
    fun selectList(index: Int) {
        val currentSection = getCurrentSection() ?: return
        
        if (index < 0 || index >= currentSection.lists.size) {
            return
        }
        
        // Get the selected list
        val selectedList = currentSection.lists[index]

        // Find the first unchecked item in the list
        var targetItemIndex = 0
        for (i in selectedList.items.indices) {
            if (!selectedList.items[i].checked) {
                targetItemIndex = i
                break
            }
        }

        // Update the state with the new list index and the first unchecked item index
        _uiState.update {
            it.copy(
                selectedListIndex = index,
                selectedItemIndex = targetItemIndex
            )
        }
    }
    
    fun selectItem(index: Int) {
        val currentList = getCurrentList() ?: return
        
        if (index < 0 || index >= currentList.items.size) {
            return
        }
        
        _uiState.update { it.copy(selectedItemIndex = index) }
    }
    
    /**
     * Calculates the next indices for navigation in the checklist structure.
     * @param advanceToNextSection Whether to automatically advance to the next section if needed
     * @param isEmergencySection Whether the current section is an emergency checklist
     * @return Triple of (nextItemIndex, nextListIndex, nextSectionIndex)
     */
    private fun calculateNextIndices(
        advanceToNextSection: Boolean = false,
        isEmergencySection: Boolean = false
    ): Triple<Int, Int, Int> {
        val currentList = getCurrentList() ?: return Triple(
            _uiState.value.selectedItemIndex,
            _uiState.value.selectedListIndex,
            _uiState.value.selectedSectionIndex
        )

        val currentSection = getCurrentSection() ?: return Triple(
            _uiState.value.selectedItemIndex,
            _uiState.value.selectedListIndex,
            _uiState.value.selectedSectionIndex
        )

        var nextItemIndex = _uiState.value.selectedItemIndex + 1
        var nextListIndex = _uiState.value.selectedListIndex
        var nextSectionIndex = _uiState.value.selectedSectionIndex

        // Check if we need to move to the next list
        if (nextItemIndex >= currentList.items.size) {
            // For emergency sections, stay on the last item of the current list
            if (isEmergencySection) {
                nextItemIndex = currentList.items.size - 1
            } else {
                nextItemIndex = 0
                nextListIndex++

                // Check if we need to move to the next section
                if (nextListIndex >= currentSection.lists.size) {
                    nextListIndex = 0

                    // Only try to advance to next section if the flag is set
                    if (advanceToNextSection) {
                        val checklist = _uiState.value.checklist ?: return Triple(
                            _uiState.value.selectedItemIndex,
                            _uiState.value.selectedListIndex,
                            _uiState.value.selectedSectionIndex
                        )

                        // Check if there's a next section and move to it only if it's of type "checklist"
                        if (nextSectionIndex < checklist.sections.size - 1) {
                            // Find next checklist-type section
                            var foundChecklistSection = false
                            for (i in nextSectionIndex + 1 until checklist.sections.size) {
                                if (checklist.sections[i].type == Constants.SECTION_TYPE_CHECKLIST) {
                                    nextSectionIndex = i
                                    foundChecklistSection = true
                                    Log.d(Constants.LOG_TAG_CHECKLIST_VIEW_MODEL,
                                        getApplication<Application>().getString(R.string.log_moving_to_section, i))
                                    break
                                }
                            }

                            // If no checklist section found, stay at current section
                            if (!foundChecklistSection) {
                                nextSectionIndex = _uiState.value.selectedSectionIndex
                            }
                        }
                    }
                }
            }
        }

        return Triple(nextItemIndex, nextListIndex, nextSectionIndex)
    }

    /**
     * Updates an item in the checklist more efficiently by using index-based updates rather than
     * recreating the entire hierarchy.
     *
     * @param sectionIndex Index of the section containing the item
     * @param listIndex Index of the list containing the item
     * @param itemIndex Index of the item to update
     * @param update Lambda that takes the current item and returns an updated one
     * @return Updated checklist or null if any index is invalid
     */
    private fun updateItemInChecklist(
        sectionIndex: Int,
        listIndex: Int,
        itemIndex: Int,
        update: (ChecklistItem) -> ChecklistItem
    ): Checklist? {
        val checklist = _uiState.value.checklist ?: return null

        // Validate indices
        if (sectionIndex < 0 || sectionIndex >= checklist.sections.size) return null

        val section = checklist.sections[sectionIndex]
        if (listIndex < 0 || listIndex >= section.lists.size) return null

        val list = section.lists[listIndex]
        if (itemIndex < 0 || itemIndex >= list.items.size) return null

        // Get the current item
        val item = list.items[itemIndex]

        // Apply the update function to get the new item
        val updatedItem = update(item)

        // Create a new checklist with the updated item
        return checklist.copy(
            sections = checklist.sections.toMutableList().apply {
                this[sectionIndex] = this[sectionIndex].copy(
                    lists = this[sectionIndex].lists.toMutableList().apply {
                        this[listIndex] = this[listIndex].copy(
                            items = this[listIndex].items.toMutableList().apply {
                                this[itemIndex] = updatedItem
                            }
                        )
                    }
                )
            }
        )
    }

    fun checkCurrentItem() {
        val currentSection = getCurrentSection() ?: return
        val isEmergencySection = currentSection.type.equals("emergency", ignoreCase = true) == true

        // Use the efficient update function
        val updatedChecklist = updateItemInChecklist(
            _uiState.value.selectedSectionIndex,
            _uiState.value.selectedListIndex,
            _uiState.value.selectedItemIndex
        ) { item ->
            item.copy(checked = true)
        } ?: return

        // Find the next unchecked item
        var nextItemIndex = _uiState.value.selectedItemIndex
        var nextListIndex = _uiState.value.selectedListIndex
        var nextSectionIndex = _uiState.value.selectedSectionIndex

        if (!isEmergencySection) {
            // For non-emergency sections, find the next unchecked item (which could be in a different list or section)
            val checklist = updatedChecklist
            var foundUncheckedItem = false

            // First look in the current list for unchecked items after the current one
            val currentList = currentSection.lists[nextListIndex]
            for (i in nextItemIndex + 1 until currentList.items.size) {
                val itemType = currentList.items[i].type.lowercase()
                // Skip items that are labels, notes, cautions, or warnings
                if (itemType == "label" || itemType == "note" || itemType == "caution" || itemType == "warning") {
                    continue
                }
                if (!currentList.items[i].checked) {
                    nextItemIndex = i
                    foundUncheckedItem = true
                    break
                }
            }

            // If not found, look in subsequent lists in this section
            if (!foundUncheckedItem) {
                listLoop@ for (listIdx in nextListIndex + 1 until currentSection.lists.size) {
                    val list = currentSection.lists[listIdx]
                    for (itemIdx in list.items.indices) {
                        val itemType = list.items[itemIdx].type.lowercase()
                        // Skip items that are labels, notes, cautions, or warnings
                        if (itemType == "label" || itemType == "note" || itemType == "caution" || itemType == "warning") {
                            continue
                        }
                        if (!list.items[itemIdx].checked) {
                            nextListIndex = listIdx
                            nextItemIndex = itemIdx
                            foundUncheckedItem = true
                            break@listLoop
                        }
                    }
                }
            }

            // If still not found and we're allowed to advance to next section, look in subsequent sections
            if (!foundUncheckedItem) {
                sectionLoop@ for (sectionIdx in nextSectionIndex + 1 until checklist.sections.size) {
                    val section = checklist.sections[sectionIdx]
                    // Only consider checklist-type sections
                    if (section.type == Constants.SECTION_TYPE_CHECKLIST) {
                        for (listIdx in section.lists.indices) {
                            val list = section.lists[listIdx]
                            for (itemIdx in list.items.indices) {
                                val itemType = list.items[itemIdx].type.lowercase()
                                // Skip items that are labels, notes, cautions, or warnings
                                if (itemType == "label" || itemType == "note" || itemType == "caution" || itemType == "warning") {
                                    continue
                                }
                                if (!list.items[itemIdx].checked) {
                                    nextSectionIndex = sectionIdx
                                    nextListIndex = listIdx
                                    nextItemIndex = itemIdx
                                    foundUncheckedItem = true
                                    break@sectionLoop
                                }
                            }
                        }
                    }
                }
            }

            // If still haven't found an unchecked item, fall back to the default behavior
            // which is to move to the next item in sequence, but skip labels, notes, cautions, and warnings
            if (!foundUncheckedItem) {
                val (nextItem, nextList, nextSection) = calculateNextIndices(
                    advanceToNextSection = true,
                    isEmergencySection = false
                )
                nextItemIndex = nextItem
                nextListIndex = nextList
                nextSectionIndex = nextSection

                // If the item we're navigating to is a label/note/caution/warning, try to find the next actionable item
                var foundActionableItem = false
                if (nextSectionIndex < updatedChecklist.sections.size) {
                    val nextSectionRef = updatedChecklist.sections[nextSectionIndex]
                    if (nextListIndex < nextSectionRef.lists.size) {
                        val nextListRef = nextSectionRef.lists[nextListIndex]
                        // Try to find next actionable item in this list
                        for (i in nextItemIndex until nextListRef.items.size) {
                            val itemType = nextListRef.items[i].type.lowercase()
                            if (itemType != "label" && itemType != "note" && itemType != "caution" && itemType != "warning") {
                                nextItemIndex = i
                                foundActionableItem = true
                                break
                            }
                        }

                        // If not found in this list, try in subsequent lists and sections
                        if (!foundActionableItem) {
                            // Search in remaining lists of this section
                            listLoop@ for (listIdx in nextListIndex + 1 until nextSectionRef.lists.size) {
                                val list = nextSectionRef.lists[listIdx]
                                for (itemIdx in list.items.indices) {
                                    val itemType = list.items[itemIdx].type.lowercase()
                                    if (itemType != "label" && itemType != "note" && itemType != "caution" && itemType != "warning") {
                                        nextListIndex = listIdx
                                        nextItemIndex = itemIdx
                                        foundActionableItem = true
                                        break@listLoop
                                    }
                                }
                            }

                            // If still not found, look in subsequent sections
                            if (!foundActionableItem) {
                                sectionLoop@ for (sectionIdx in nextSectionIndex + 1 until updatedChecklist.sections.size) {
                                    val section = updatedChecklist.sections[sectionIdx]
                                    if (section.type == Constants.SECTION_TYPE_CHECKLIST) {
                                        for (listIdx in section.lists.indices) {
                                            val list = section.lists[listIdx]
                                            for (itemIdx in list.items.indices) {
                                                val itemType = list.items[itemIdx].type.lowercase()
                                                if (itemType != "label" && itemType != "note" && itemType != "caution" && itemType != "warning") {
                                                    nextSectionIndex = sectionIdx
                                                    nextListIndex = listIdx
                                                    nextItemIndex = itemIdx
                                                    foundActionableItem = true
                                                    break@sectionLoop
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // For emergency sections, find the next unchecked item but stay within the section
            var foundUncheckedItem = false

            // First look in the current list for unchecked items after the current one
            val currentList = currentSection.lists[nextListIndex]
            for (i in nextItemIndex + 1 until currentList.items.size) {
                val itemType = currentList.items[i].type.lowercase()
                // Skip items that are labels, notes, cautions, or warnings
                if (itemType == "label" || itemType == "note" || itemType == "caution" || itemType == "warning") {
                    continue
                }
                if (!currentList.items[i].checked) {
                    nextItemIndex = i
                    foundUncheckedItem = true
                    break
                }
            }

            // If not found, look in subsequent lists in this emergency section
            if (!foundUncheckedItem) {
                listLoop@ for (listIdx in nextListIndex + 1 until currentSection.lists.size) {
                    val list = currentSection.lists[listIdx]
                    for (itemIdx in list.items.indices) {
                        val itemType = list.items[itemIdx].type.lowercase()
                        // Skip items that are labels, notes, cautions, or warnings
                        if (itemType == "label" || itemType == "note" || itemType == "caution" || itemType == "warning") {
                            continue
                        }
                        if (!list.items[itemIdx].checked) {
                            nextListIndex = listIdx
                            nextItemIndex = itemIdx
                            foundUncheckedItem = true
                            break@listLoop
                        }
                    }
                }
            }

            // If still haven't found an unchecked item in this emergency section,
            // fall back to the default sequential behavior but stay within the section
            if (!foundUncheckedItem) {
                val (nextItem, nextList, _) = calculateNextIndices(
                    advanceToNextSection = false,
                    isEmergencySection = true
                )
                nextItemIndex = nextItem
                nextListIndex = nextList
                // Don't change section for emergency checklists

                // If the next item is a label/note/caution/warning, try to find the next actionable item
                var foundActionableItem = false
                val nextSectionRef = updatedChecklist.sections[nextSectionIndex]
                if (nextListIndex < nextSectionRef.lists.size) {
                    val nextListRef = nextSectionRef.lists[nextListIndex]
                    // Try to find next actionable item in this list
                    for (i in nextItemIndex until nextListRef.items.size) {
                        val itemType = nextListRef.items[i].type.lowercase()
                        if (itemType != "label" && itemType != "note" && itemType != "caution" && itemType != "warning") {
                            nextItemIndex = i
                            foundActionableItem = true
                            break
                        }
                    }

                    // If not found in this list, try in subsequent lists (staying within section)
                    if (!foundActionableItem) {
                        listLoop@ for (listIdx in nextListIndex + 1 until nextSectionRef.lists.size) {
                            val list = nextSectionRef.lists[listIdx]
                            for (itemIdx in list.items.indices) {
                                val itemType = list.items[itemIdx].type.lowercase()
                                if (itemType != "label" && itemType != "note" && itemType != "caution" && itemType != "warning") {
                                    nextListIndex = listIdx
                                    nextItemIndex = itemIdx
                                    foundActionableItem = true
                                    break@listLoop
                                }
                            }
                        }
                    }
                }
            }
        }

        _uiState.update {
            it.copy(
                checklist = updatedChecklist,
                selectedItemIndex = nextItemIndex,
                selectedListIndex = nextListIndex,
                selectedSectionIndex = nextSectionIndex
            )
        }
    }
    
    fun skipCurrentItem() {
        // Calculate next navigation indices (without advancing to next section)
        val (nextItemIndex, nextListIndex, _) = calculateNextIndices(advanceToNextSection = false)

        _uiState.update {
            it.copy(
                selectedItemIndex = nextItemIndex,
                selectedListIndex = nextListIndex
            )
        }
    }
    
    fun toggleItemChecked() {
        getCurrentItem() ?: return

        // Use the efficient update function instead of manual recreation
        val updatedChecklist = updateItemInChecklist(
            _uiState.value.selectedSectionIndex,
            _uiState.value.selectedListIndex,
            _uiState.value.selectedItemIndex
        ) { item ->
            item.copy(checked = !item.checked)
        } ?: return

        _uiState.update {
            it.copy(checklist = updatedChecklist)
        }
    }

    private fun getCurrentSection(): ChecklistSection? {
        val checklist = _uiState.value.checklist ?: return null
        val index = _uiState.value.selectedSectionIndex
        return if (index < checklist.sections.size) checklist.sections[index] else null
    }
    
    private fun getCurrentList(): ChecklistList? {
        val section = getCurrentSection() ?: return null
        val index = _uiState.value.selectedListIndex
        return if (index < section.lists.size) section.lists[index] else null
    }
    
    private fun getCurrentItem(): ChecklistItem? {
        val list = getCurrentList() ?: return null
        val index = _uiState.value.selectedItemIndex
        return if (index < list.items.size) list.items[index] else null
    }
}

