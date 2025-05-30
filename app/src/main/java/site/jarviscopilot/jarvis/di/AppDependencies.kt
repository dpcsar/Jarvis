package site.jarviscopilot.jarvis.di

import android.app.Application
import android.content.Context
import site.jarviscopilot.jarvis.data.repository.ChecklistRepository
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import site.jarviscopilot.jarvis.util.UserPreferences
import site.jarviscopilot.jarvis.viewmodel.ChecklistViewModelFactory
import site.jarviscopilot.jarvis.viewmodel.MainViewModelFactory
import site.jarviscopilot.jarvis.viewmodel.SettingsViewModelFactory

/**
 * Dependency provider for the application
 * This is a simple manual DI implementation that can be replaced with a more robust solution like Hilt or Koin in the future
 */
object AppDependencies {

    /**
     * Provides a ChecklistRepository instance
     */
    fun provideChecklistRepository(context: Context): IChecklistRepository {
        return ChecklistRepository(context)
    }

    /**
     * Provides a ChecklistStateManager instance
     */
    fun provideChecklistStateManager(context: Context): ChecklistStateManager {
        return ChecklistStateManager(context)
    }

    /**
     * Provides a MainViewModelFactory instance
     */
    fun provideMainViewModelFactory(context: Context): MainViewModelFactory {
        val repository = provideChecklistRepository(context)
        val stateManager = provideChecklistStateManager(context)
        return MainViewModelFactory(repository, stateManager)
    }

    /**
     * Provides a SettingsViewModelFactory instance
     */
    fun provideSettingsViewModelFactory(context: Context): SettingsViewModelFactory {
        val repository = provideChecklistRepository(context)
        val userPreferences = UserPreferences.getInstance(context)
        return SettingsViewModelFactory(repository, userPreferences)
    }

    /**
     * Provides a ChecklistViewModelFactory instance
     *
     * @param application The application context
     * @param checklistName The name of the checklist to load
     * @param resumeFromSaved Whether to resume from saved state
     */
    fun provideChecklistViewModelFactory(
        application: Application,
        checklistName: String,
        resumeFromSaved: Boolean
    ): ChecklistViewModelFactory {
        val repository = provideChecklistRepository(application)
        val stateManager = provideChecklistStateManager(application)
        return ChecklistViewModelFactory(
            application,
            repository,
            stateManager,
            checklistName,
            resumeFromSaved
        )
    }
}
