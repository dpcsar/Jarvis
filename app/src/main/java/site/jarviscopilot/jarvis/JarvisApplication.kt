package site.jarviscopilot.jarvis

import android.app.Application

/**
 * Custom Application class for the Jarvis app
 * Serves as the entry point for initializing application-wide components
 */
class JarvisApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize application-wide components
        initializeDependencies()
    }

    /**
     * Initialize all application-wide dependencies
     */
    private fun initializeDependencies() {
        // Any additional initialization can be done here
        // For example:
        // - Logging frameworks
        // - Analytics tools
        // - Crash reporting
    }
}
