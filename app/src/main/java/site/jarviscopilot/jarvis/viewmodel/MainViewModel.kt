package site.jarviscopilot.jarvis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import site.jarviscopilot.jarvis.ui.state.MainUiState

class MainViewModel(
    private val checklistRepository: IChecklistRepository,
    private val checklistStateManager: ChecklistStateManager
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow(MainUiState(isLoading = true))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadChecklists()
    }

    fun loadChecklists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val checklists = checklistRepository.getAvailableChecklists()
                val resumableChecklists = checklistStateManager.getSavedChecklistNames()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        checklists = checklists,
                        resumableChecklists = resumableChecklists,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load checklists"
                    )
                }
            }
        }
    }

    fun clearChecklistStateQuietly(checklistId: String) {
        viewModelScope.launch {
            checklistStateManager.clearChecklistState(checklistId)
            // Update the resumable checklists after clearing one
            _uiState.update {
                it.copy(resumableChecklists = checklistStateManager.getSavedChecklistNames())
            }
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
