package site.jarviscopilot.jarvis.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import site.jarviscopilot.jarvis.data.model.Checklist
import site.jarviscopilot.jarvis.data.repository.ChecklistRepository

class ChecklistViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChecklistRepository(application.applicationContext)

    var checklistNames by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedChecklist by mutableStateOf<Checklist?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadChecklistNames()
    }

    private fun loadChecklistNames() {
        viewModelScope.launch {
            isLoading = true
            try {
                checklistNames = repository.loadChecklistNames()
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Failed to load checklists: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadChecklist(filename: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                // Convert display name back to filename format
                val formattedFilename = "cl_${filename.lowercase().replace(" ", "_")}.json"
                selectedChecklist = repository.loadChecklist(formattedFilename)
                errorMessage = selectedChecklist?.let { null } ?: "Failed to load checklist"
            } catch (e: Exception) {
                errorMessage = "Error loading checklist: ${e.message}"
                selectedChecklist = null
            } finally {
                isLoading = false
            }
        }
    }

    fun clearSelectedChecklist() {
        selectedChecklist = null
    }
}
