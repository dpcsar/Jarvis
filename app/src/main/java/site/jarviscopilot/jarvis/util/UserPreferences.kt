package site.jarviscopilot.jarvis.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Enum class for theme mode options
enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

// Handles storing and retrieving user preferences
class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // StateFlow for theme mode updates
    private val _themeModeFlow = MutableStateFlow(getThemeMode())
    val themeModeFlow: StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    fun isVoiceControlEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VOICE_CONTROL_ENABLED, false)
    }

    fun setVoiceControlEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_VOICE_CONTROL_ENABLED, enabled)
        }
    }

    fun getThemeMode(): ThemeMode {
        val themeModeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(themeModeString ?: ThemeMode.SYSTEM.name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit {
            putString(KEY_THEME_MODE, mode.name)
        }
        // Update the StateFlow when theme mode changes
        _themeModeFlow.value = mode
    }

    companion object {
        private const val PREFS_NAME = "jarvis_preferences"
        private const val KEY_VOICE_CONTROL_ENABLED = "voice_control_enabled"
        private const val KEY_THEME_MODE = "theme_mode"

        // Singleton instance
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
