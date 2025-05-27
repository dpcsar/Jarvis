package site.jarviscopilot.jarvis.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import androidx.core.content.edit

/**
 * Manages the state of checklists to support resume functionality
 */
class ChecklistStateManager(context: Context) {

    private val preferences: SharedPreferences = context.getSharedPreferences("checklist_state", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Save the state of a checklist
     */
    fun saveChecklistState(state: ChecklistState) {
        // Create a unique key for this checklist's state
        val checklistKey = "checklist_${state.checklistFilename}"

        // Store the entire state as a JSON string
        val stateJson = json.encodeToString(state)
        preferences.edit {
            putString(checklistKey, stateJson)

            // Also maintain a list of all checklists with saved state
            val savedChecklists = getSavedChecklistFilenames().toMutableSet()
            savedChecklists.add(state.checklistFilename)
            putStringSet("saved_checklists", savedChecklists)
        }
    }

    /**
     * Get the saved state for a specific checklist, or null if no state is saved
     */
    fun getChecklistState(checklistFilename: String): ChecklistState? {
        val checklistKey = "checklist_${checklistFilename}"
        val stateJson = preferences.getString(checklistKey, null) ?: return null

        return try {
            json.decodeFromString<ChecklistState>(stateJson)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get a list of all checklist filenames that have saved state
     */
    fun getSavedChecklistFilenames(): Set<String> {
        return preferences.getStringSet("saved_checklists", emptySet()) ?: emptySet()
    }

    /**
     * Clear the saved state for a specific checklist
     */
    fun clearChecklistState(checklistFilename: String) {
        val checklistKey = "checklist_${checklistFilename}"

        preferences.edit {
            remove(checklistKey)

            // Update the list of saved checklists
            val savedChecklists = getSavedChecklistFilenames().toMutableSet()
            savedChecklists.remove(checklistFilename)
            putStringSet("saved_checklists", savedChecklists)
        }
    }

}

/**
 * Represents the state of a checklist that can be saved and restored
 */
@Serializable
data class ChecklistState(
    val checklistFilename: String,
    val sectionIndex: Int,
    val listIndex: Int,
    val activeItemIndex: Int,
    val completedItems: List<List<List<Int>>> // Completed items per section and list
)
