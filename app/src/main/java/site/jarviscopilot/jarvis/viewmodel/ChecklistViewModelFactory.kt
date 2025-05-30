package site.jarviscopilot.jarvis.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager

/**
 * Factory for creating ViewModels with dependencies.
 */
class ChecklistViewModelFactory(
    private val application: Application,
    private val repository: IChecklistRepository,
    private val stateManager: ChecklistStateManager,
    private val checklistName: String,
    private val resumeFromSaved: Boolean
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChecklistViewModel::class.java)) {
            return ChecklistViewModel(
                application = application,
                repository = repository,
                stateManager = stateManager,
                checklistName = checklistName,
                resumeFromSaved = resumeFromSaved
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
