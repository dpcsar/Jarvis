package site.jarviscopilot.jarvis.viewmodel

import android.app.Application
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
    val selectedListIndex: Int = 0,
    val selectedSectionIndex: Int = 0,
    val selectedItemIndex: Int = 0,
    val currentLocalTime: String = "",
    val currentUtcTime: String = "",
    val currentPhase: String = "PreFlight"
)

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = ChecklistRepository(application)
    
    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()
    
    init {
        loadChecklist()
        startTimeUpdates()
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
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            checklist = checklist,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load checklist: ${exception.message}"
                        )
                    }
                }
        }
    }
    
    fun selectList(index: Int) {
        if (index < 0 || _uiState.value.checklist?.children?.size?.let { index >= it } == true) {
            return
        }
        
        _uiState.update { 
            it.copy(
                selectedListIndex = index,
                selectedSectionIndex = 0,
                selectedItemIndex = 0
            )
        }
    }
    
    fun selectSection(index: Int) {
        val currentList = getCurrentList() ?: return
        
        if (index < 0 || index >= currentList.children.size) {
            return
        }
        
        _uiState.update { 
            it.copy(
                selectedSectionIndex = index,
                selectedItemIndex = 0
            )
        }
    }
    
    fun selectItem(index: Int) {
        val currentSection = getCurrentSection() ?: return
        
        if (index < 0 || index >= currentSection.children.size) {
            return
        }
        
        _uiState.update { it.copy(selectedItemIndex = index) }
    }
    
    fun checkCurrentItem() {
        val currentItem = getCurrentItem() ?: return
        val currentList = getCurrentList() ?: return
        val currentSection = getCurrentSection() ?: return
        
        // Since our model is immutable, we need to create new copies with the updated state
        val updatedItem = currentItem.copy(checked = true)
        
        // Update the section with the new item
        val updatedSectionItems = currentSection.children.toMutableList()
        updatedSectionItems[_uiState.value.selectedItemIndex] = updatedItem
        val updatedSection = currentSection.copy(children = updatedSectionItems)
        
        // Update the list with the new section
        val updatedSections = currentList.children.toMutableList()
        updatedSections[_uiState.value.selectedSectionIndex] = updatedSection
        val updatedList = currentList.copy(children = updatedSections)
        
        // Update the checklist with the new list
        val checklist = _uiState.value.checklist ?: return
        val updatedLists = checklist.children.toMutableList()
        updatedLists[_uiState.value.selectedListIndex] = updatedList
        val updatedChecklist = checklist.copy(children = updatedLists)
        
        // Move to the next item
        var nextItemIndex = _uiState.value.selectedItemIndex + 1
        var nextSectionIndex = _uiState.value.selectedSectionIndex
        
        // Check if we need to move to the next section
        if (nextItemIndex >= currentSection.children.size) {
            nextItemIndex = 0
            nextSectionIndex++
            
            // Check if we need to move to the next list
            if (nextSectionIndex >= currentList.children.size) {
                nextSectionIndex = 0
                // We don't automatically move to the next list
            }
        }
        
        _uiState.update { 
            it.copy(
                checklist = updatedChecklist,
                selectedItemIndex = nextItemIndex,
                selectedSectionIndex = nextSectionIndex
            )
        }
    }
    
    fun skipCurrentItem() {
        val currentSection = getCurrentSection() ?: return
        
        // Move to the next item without marking as complete
        var nextItemIndex = _uiState.value.selectedItemIndex + 1
        var nextSectionIndex = _uiState.value.selectedSectionIndex
        
        // Check if we need to move to the next section
        if (nextItemIndex >= currentSection.children.size) {
            nextItemIndex = 0
            nextSectionIndex++
            
            val currentList = getCurrentList() ?: return
            
            // Check if we need to move to the next list
            if (nextSectionIndex >= currentList.children.size) {
                nextSectionIndex = 0
                // We don't automatically move to the next list
            }
        }
        
        _uiState.update { 
            it.copy(
                selectedItemIndex = nextItemIndex,
                selectedSectionIndex = nextSectionIndex
            )
        }
    }
    
    fun setPhase(phase: String) {
        _uiState.update { it.copy(currentPhase = phase) }
    }
    
    private fun getCurrentList(): ChecklistList? {
        val checklist = _uiState.value.checklist ?: return null
        val index = _uiState.value.selectedListIndex
        return if (index < checklist.children.size) checklist.children[index] else null
    }
    
    private fun getCurrentSection(): ChecklistSection? {
        val list = getCurrentList() ?: return null
        val index = _uiState.value.selectedSectionIndex
        return if (index < list.children.size) list.children[index] else null
    }
    
    private fun getCurrentItem(): ChecklistItem? {
        val section = getCurrentSection() ?: return null
        val index = _uiState.value.selectedItemIndex
        return if (index < section.children.size) section.children[index] else null
    }
}