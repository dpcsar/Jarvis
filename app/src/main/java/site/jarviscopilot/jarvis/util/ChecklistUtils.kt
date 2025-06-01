package site.jarviscopilot.jarvis.util

import site.jarviscopilot.jarvis.ui.components.ChecklistItemType

/**
 * Utility class for checklist operations.
 */
object ChecklistUtils {

    /**
     * Converts a string listItemType to a ChecklistItemType enum
     */
    fun convertToItemType(listItemType: String): ChecklistItemType {
        return when (listItemType.lowercase()) {
            "task" -> ChecklistItemType.TASK
            "note" -> ChecklistItemType.NOTE
            "label" -> ChecklistItemType.LABEL
            "caution" -> ChecklistItemType.CAUTION
            "warning" -> ChecklistItemType.WARNING
            "reference" -> ChecklistItemType.REFERENCE
            "referencenote" -> ChecklistItemType.REFERENCENOTE
            else -> ChecklistItemType.TASK // Default to TASK for any unknown types
        }
    }

    /**
     * Determines if a checklist is complete
    fun isChecklistComplete(completed: List<Int>, total: Int): Boolean {
    return completed.size == total && total > 0
    }
     */
}
