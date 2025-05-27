package site.jarviscopilot.jarvis.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import site.jarviscopilot.jarvis.data.ChecklistRepository
import site.jarviscopilot.jarvis.data.ChecklistStateManager

/**
 * Factory for creating ViewModels with dependencies.
 */
class ChecklistViewModelFactory(
    private val context: Context,
    private val checklistName: String,
    private val resumeFromSaved: Boolean
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChecklistViewModel::class.java)) {
            // Use application context to prevent memory leaks
            val appContext = context.applicationContext

            // Create dependencies with application context
            val repository = ChecklistRepository(appContext)
            val stateManager = ChecklistStateManager(appContext)

            return ChecklistViewModel(
                repository = repository,
                stateManager = stateManager,
                checklistName = checklistName,
                resumeFromSaved = resumeFromSaved
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
