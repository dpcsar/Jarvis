package site.jarviscopilot.jarvis.di

import android.content.Context
import site.jarviscopilot.jarvis.data.repository.ChecklistRepository
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager

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
}
