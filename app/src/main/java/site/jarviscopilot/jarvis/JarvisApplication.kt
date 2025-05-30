package site.jarviscopilot.jarvis

import android.app.Application
import site.jarviscopilot.jarvis.util.TtsManager

/**
 * Custom Application class for the Jarvis app
 * Serves as the entry point for initializing application-wide components
 */
class JarvisApplication : Application() {

    private lateinit var ttsManager: TtsManager

    override fun onCreate() {
        super.onCreate()

        // Initialize application-wide components
        initializeDependencies()
    }

    /**
     * Initialize all application-wide dependencies
     */
    private fun initializeDependencies() {
        // Initialize TTS Manager
        ttsManager = TtsManager.getInstance(applicationContext)

        // Any additional initialization can be done here
        // For example:
        // - Logging frameworks
        // - Analytics tools
        // - Crash reporting
    }

    override fun onTerminate() {
        // Clean up resources
        ttsManager.shutdown()
        super.onTerminate()
    }
}
