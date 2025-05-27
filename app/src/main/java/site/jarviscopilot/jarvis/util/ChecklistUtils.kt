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
            else -> ChecklistItemType.TASK // Default to TASK for any unknown types
        }
    }

    /**
     * Formats a progress value as a percentage
     */
    fun formatProgress(completed: Int, total: Int): String {
        if (total == 0) return "0%"
        val percentage = (completed.toFloat() / total.toFloat() * 100).toInt()
        return "$percentage%"
    }
}
