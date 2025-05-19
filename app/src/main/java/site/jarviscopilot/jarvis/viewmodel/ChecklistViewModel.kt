package site.jarviscopilot.jarvis.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.model.Checklist
import site.jarviscopilot.jarvis.model.ChecklistItem
import site.jarviscopilot.jarvis.model.ChecklistList
import site.jarviscopilot.jarvis.model.ChecklistSection
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
        Log.d("ChecklistViewModel", "ViewModel initialized")
    }
    
    private fun startTimeUpdates() {
        viewModelScope.launch {
            while (true) {
                updateTime()
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }
    
    private fun updateTime() {
        val now = LocalDateTime.now()
        val utcNow = LocalDateTime.now(ZoneOffset.UTC)
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        
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
                    Log.d("ChecklistViewModel", "Checklist loaded successfully: ${checklist.name}, ${checklist.sections.size} sections")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            checklist = checklist,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    Log.e("ChecklistViewModel", "Failed to load checklist", exception)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load checklist: ${exception.message}"
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
        
        _uiState.update { 
            it.copy(
                selectedSectionIndex = index,
                selectedListIndex = 0,
                selectedItemIndex = 0
            )
        }
    }
    
    fun selectList(index: Int) {
        val currentSection = getCurrentSection() ?: return
        
        if (index < 0 || index >= currentSection.lists.size) {
            return
        }
        
        _uiState.update { 
            it.copy(
                selectedListIndex = index,
                selectedItemIndex = 0
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
    
    fun checkCurrentItem() {
        val currentItem = getCurrentItem() ?: return
        val currentList = getCurrentList() ?: return
        val currentSection = getCurrentSection() ?: return
        val checklist = _uiState.value.checklist ?: return

        // Since our model is immutable, we need to create new copies with the updated state
        val updatedItem = currentItem.copy(checked = true)
        
        // Update the list with the new item
        val updatedListItems = currentList.items.toMutableList()
        updatedListItems[_uiState.value.selectedItemIndex] = updatedItem
        val updatedList = currentList.copy(items = updatedListItems)
        
        // Update the section with the new list
        val updatedLists = currentSection.lists.toMutableList()
        updatedLists[_uiState.value.selectedListIndex] = updatedList
        val updatedSection = currentSection.copy(lists = updatedLists)
        
        // Update the checklist with the new section
        val updatedSections = checklist.sections.toMutableList()
        updatedSections[_uiState.value.selectedSectionIndex] = updatedSection
        val updatedChecklist = checklist.copy(sections = updatedSections)
        
        // Move to the next item
        var nextItemIndex = _uiState.value.selectedItemIndex + 1
        var nextListIndex = _uiState.value.selectedListIndex
        var nextSectionIndex = _uiState.value.selectedSectionIndex

        // Check if we need to move to the next list
        if (nextItemIndex >= currentList.items.size) {
            nextItemIndex = 0
            nextListIndex++
            
            // Check if we need to move to the next section
            if (nextListIndex >= currentSection.lists.size) {
                nextListIndex = 0

                // Check if there's a next section and move to it only if it's of type "checklist"
                if (nextSectionIndex < checklist.sections.size - 1) {
                    // Find next checklist-type section
                    var foundChecklistSection = false
                    for (i in nextSectionIndex + 1 until checklist.sections.size) {
                        if (checklist.sections[i].type == "checklist") {
                            nextSectionIndex = i
                            foundChecklistSection = true
                            Log.d("ChecklistViewModel", "Moving to next checklist section: $i")
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
        val currentList = getCurrentList() ?: return
        
        // Move to the next item without marking as complete
        var nextItemIndex = _uiState.value.selectedItemIndex + 1
        var nextListIndex = _uiState.value.selectedListIndex
        
        // Check if we need to move to the next list
        if (nextItemIndex >= currentList.items.size) {
            nextItemIndex = 0
            nextListIndex++
            
            val currentSection = getCurrentSection() ?: return
            
            // Check if we need to move to the next section
            if (nextListIndex >= currentSection.lists.size) {
                nextListIndex = 0
                // We don't automatically move to the next section
            }
        }
        
        _uiState.update { 
            it.copy(
                selectedItemIndex = nextItemIndex,
                selectedListIndex = nextListIndex
            )
        }
    }
    
    fun toggleItemChecked() {
        val currentItem = getCurrentItem() ?: return
        val currentList = getCurrentList() ?: return
        val currentSection = getCurrentSection() ?: return

        // Toggle the checked state
        val updatedItem = currentItem.copy(checked = !currentItem.checked)

        // Update the list with the new item
        val updatedListItems = currentList.items.toMutableList()
        updatedListItems[_uiState.value.selectedItemIndex] = updatedItem
        val updatedList = currentList.copy(items = updatedListItems)

        // Update the section with the new list
        val updatedLists = currentSection.lists.toMutableList()
        updatedLists[_uiState.value.selectedListIndex] = updatedList
        val updatedSection = currentSection.copy(lists = updatedLists)

        // Update the checklist with the new section
        val checklist = _uiState.value.checklist ?: return
        val updatedSections = checklist.sections.toMutableList()
        updatedSections[_uiState.value.selectedSectionIndex] = updatedSection
        val updatedChecklist = checklist.copy(sections = updatedSections)

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

