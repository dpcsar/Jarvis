package site.jarviscopilot.jarvis

import android.app.Application
import site.jarviscopilot.jarvis.data.repository.IChecklistRepository
import site.jarviscopilot.jarvis.data.source.ChecklistStateManager
import site.jarviscopilot.jarvis.di.AppDependencies

/**
 * Custom Application class for the Jarvis app
 * Serves as the entry point for initializing application-wide components
 */
class JarvisApplication : Application() {

    // Lazy-initialized application-wide dependencies
    lateinit var checklistRepository: IChecklistRepository
    lateinit var checklistStateManager: ChecklistStateManager

    override fun onCreate() {
        super.onCreate()

        // Initialize application-wide components
        initializeDependencies()
    }

    /**
     * Initialize all application-wide dependencies
     */
    private fun initializeDependencies() {
        // Initialize repositories and data sources
        checklistRepository = AppDependencies.provideChecklistRepository(applicationContext)
        checklistStateManager = AppDependencies.provideChecklistStateManager(applicationContext)

        // Any additional initialization can be done here
        // For example:
        // - Logging frameworks
        // - Analytics tools
        // - Crash reporting
    }
}
