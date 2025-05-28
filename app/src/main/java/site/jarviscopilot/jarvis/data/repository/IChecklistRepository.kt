package site.jarviscopilot.jarvis.data.repository

import android.net.Uri
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistInfoData
import site.jarviscopilot.jarvis.data.model.ChecklistStateData

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
    fun getAvailableChecklists(): List<ChecklistInfoData>

    /**
     * Saves the state of a checklist
     */
    fun saveChecklistState(checklistStateData: ChecklistStateData): Boolean

    /**
     * Loads a previously saved checklist state
     */
    fun loadChecklistState(checklistName: String): ChecklistStateData?

    /**
     * Imports a checklist from a URI
     */
    suspend fun importChecklist(uri: Uri): Result<ChecklistInfoData>

    /**
     * Deletes a checklist
     */
    fun deleteChecklist(checklistId: String): Boolean
}
