package site.jarviscopilot.jarvis.ui.state

import site.jarviscopilot.jarvis.data.model.ChecklistInfoData

/**
 * Represents the UI state for the main screen
 */
data class MainUiState(
    val isLoading: Boolean = false,
    val checklists: List<ChecklistInfoData> = emptyList(),
    val resumableChecklists: Set<String> = emptySet(),
    val error: String? = null
)
