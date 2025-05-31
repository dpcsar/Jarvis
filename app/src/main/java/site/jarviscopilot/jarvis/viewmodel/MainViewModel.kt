package site.jarviscopilot.jarvis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager

class MainViewModel(
    private val checklistRepository: IChecklistRepository,
    private val checklistStateManager: ChecklistStateManager
) : ViewModel() {

    // UI state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _checklists = MutableStateFlow<List<ChecklistInfoData>>(emptyList())
    val checklists: StateFlow<List<ChecklistInfoData>> = _checklists.asStateFlow()

    private val _resumableChecklists = MutableStateFlow<Set<String>>(emptySet())
    val resumableChecklists: StateFlow<Set<String>> = _resumableChecklists.asStateFlow()

    init {
        loadChecklists()
    }

    fun loadChecklists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _checklists.value = checklistRepository.getAvailableChecklists()
                _resumableChecklists.value = checklistStateManager.getSavedChecklistNames()
            } catch (_: Exception) {
                // Handle error if needed
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChecklistStateQuietly(checklistId: String) {
        viewModelScope.launch {
            checklistStateManager.clearChecklistState(checklistId)
        }
    }
}

class MainViewModelFactory(
    private val checklistRepository: IChecklistRepository,
    private val checklistStateManager: ChecklistStateManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(checklistRepository, checklistStateManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
