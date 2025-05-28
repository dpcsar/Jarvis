package site.jarviscopilot.jarvis.data.repository

import android.net.Uri
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistInfo
import site.jarviscopilot.jarvis.data.model.ChecklistState

/**
 * Repository interface for checklist operations
 */
interface IChecklistRepository {
    /**
     * Loads a checklist from its filename
     */
    fun loadChecklist(checklistName: String): ChecklistData?

    /**
     * Gets all available checklists
     */
    fun getAvailableChecklists(): List<ChecklistInfo>

    /**
     * Saves the state of a checklist
     */
    fun saveChecklistState(checklistState: ChecklistState): Boolean

    /**
     * Loads a previously saved checklist state
     */
    fun loadChecklistState(checklistName: String): ChecklistState?

    /**
     * Imports a checklist from a URI
     */
    suspend fun importChecklist(uri: Uri): Result<ChecklistInfo>

    /**
     * Deletes a checklist
     */
    fun deleteChecklist(checklistId: String): Boolean
}
