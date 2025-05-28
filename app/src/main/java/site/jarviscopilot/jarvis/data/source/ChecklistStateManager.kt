package site.jarviscopilot.jarvis.data.source

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import site.jarviscopilot.jarvis.data.model.ChecklistState

/**
 * Manages the state of checklists to support resume functionality
 */
class ChecklistStateManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("checklist_state", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Save the state of a checklist
     */
    fun saveChecklistState(state: ChecklistState): Boolean {
        return try {
            // Create a unique key for this checklist's state
            val checklistKey = "checklist_${state.checklistName}"

            // Store the entire state as a JSON string
            val stateJson = json.encodeToString(ChecklistState.serializer(), state)
            preferences.edit {
                putString(checklistKey, stateJson)

                // Also maintain a list of all checklists with saved state
                val savedChecklists = getSavedChecklistNames().toMutableSet()
                savedChecklists.add(state.checklistName)
                putStringSet("saved_checklists", savedChecklists)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get the saved state for a specific checklist, or null if no state is saved
     */
    fun getChecklistState(checklistName: String): ChecklistState? {
        val checklistKey = "checklist_${checklistName}"
        val stateJson = preferences.getString(checklistKey, null) ?: return null

        return try {
            json.decodeFromString(ChecklistState.serializer(), stateJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get a list of all checklist names that have saved state
     */
    fun getSavedChecklistNames(): Set<String> {
        return preferences.getStringSet("saved_checklists", emptySet()) ?: emptySet()
    }

    /**
     * Clear the saved state for a specific checklist
     */
    fun clearChecklistState(checklistName: String) {
        val checklistKey = "checklist_${checklistName}"

        preferences.edit {
            remove(checklistKey)

            // Update the list of saved checklists
            val savedChecklists = getSavedChecklistNames().toMutableSet()
            savedChecklists.remove(checklistName)
            putStringSet("saved_checklists", savedChecklists)
        }
    }
}
