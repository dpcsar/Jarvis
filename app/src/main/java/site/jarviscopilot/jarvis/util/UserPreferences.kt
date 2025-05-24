package site.jarviscopilot.jarvis.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Handles storing and retrieving user preferences
class UserPreferences(context: Context) {

    private val sharedPreferences: SharedPreferences = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // StateFlow for night mode updates
    private val _nightModeFlow = MutableStateFlow(isNightModeEnabled())
    val nightModeFlow: StateFlow<Boolean> = _nightModeFlow.asStateFlow()

    fun isVoiceControlEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VOICE_CONTROL_ENABLED, false)
    }

    fun setVoiceControlEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_VOICE_CONTROL_ENABLED, enabled)
        }
    }

    fun isNightModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NIGHT_MODE_ENABLED, false)
    }

    fun setNightModeEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_NIGHT_MODE_ENABLED, enabled)
        }
        // Update the StateFlow when night mode changes
        _nightModeFlow.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "jarvis_preferences"
        private const val KEY_VOICE_CONTROL_ENABLED = "voice_control_enabled"
        private const val KEY_NIGHT_MODE_ENABLED = "night_mode_enabled"

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
