package site.jarviscopilot.jarvis.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.util.ThemeMode
import site.jarviscopilot.jarvis.util.UserPreferences

class SettingsViewModel(
    private val checklistRepository: IChecklistRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _checklists = MutableStateFlow<List<ChecklistInfoData>>(emptyList())
    val checklists: StateFlow<List<ChecklistInfoData>> = _checklists.asStateFlow()

    private val _toastEvent = MutableStateFlow<ToastEvent?>(null)
    val toastEvent: StateFlow<ToastEvent?> = _toastEvent.asStateFlow()

    private val _themeMode = MutableStateFlow(userPreferences.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _useVoiceControl = MutableStateFlow(userPreferences.isVoiceControlEnabled())
    val useVoiceControl: StateFlow<Boolean> = _useVoiceControl.asStateFlow()

    private val _useTts = MutableStateFlow(userPreferences.isTtsEnabled())
    val useTts: StateFlow<Boolean> = _useTts.asStateFlow()

    init {
        loadChecklists()
    }

    fun loadChecklists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _checklists.value = checklistRepository.getAvailableChecklists()
            } catch (e: Exception) {
                showToast("Error loading checklists: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importChecklist(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = checklistRepository.importChecklist(uri)
                result.fold(
                    onSuccess = { checklistInfo ->
                        showToast("Checklist '${checklistInfo.name}' imported successfully")
                        loadChecklists()
                    },
                    onFailure = { exception ->
                        showToast("Failed to import checklist: ${exception.message}")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteChecklist(checklistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = checklistRepository.deleteChecklist(checklistId)
                if (success) {
                    showToast("Checklist deleted")
                    loadChecklists()
                } else {
                    showToast("Failed to delete checklist")
                }
            } catch (e: Exception) {
                showToast("Error deleting checklist: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        userPreferences.setThemeMode(themeMode)
        _themeMode.value = themeMode
    }

    fun setVoiceControlEnabled(enabled: Boolean) {
        userPreferences.setVoiceControlEnabled(enabled)
        _useVoiceControl.value = enabled
        if (enabled) {
            showToast("Voice control enabled")
        } else {
            showToast("Voice control disabled")
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        userPreferences.setTtsEnabled(enabled)
        _useTts.value = enabled
    }

    fun startVoiceTraining() {
        viewModelScope.launch {
            try {
                // Here you would integrate with your voice training system
                // This is a placeholder implementation
                showToast("Voice training started")

                // TODO: Implement actual voice training functionality
                // For example, navigate to a training screen or start a training process
            } catch (e: Exception) {
                showToast("Error starting voice training: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        _toastEvent.value = ToastEvent(message)
    }

    fun clearToastEvent() {
        _toastEvent.value = null
    }

    data class ToastEvent(val message: String)
}

class SettingsViewModelFactory(
    private val checklistRepository: IChecklistRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(checklistRepository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
