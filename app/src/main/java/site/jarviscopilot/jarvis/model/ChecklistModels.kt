package site.jarviscopilot.jarvis.model

data class Checklist(
    val id: String,
    val name: String,
    val description: String,
    val genre: String,
    val publisher: String,
    val tags: List<String>,
    val children: List<ChecklistList>,
    val speakMode: String,
    val checklistView: String,
    val image: String?
)

data class ChecklistList(
    val id: String,
    val name: String,
    val type: String,
    val completionState: String,
    val visible: Boolean,
    val children: List<ChecklistSection>,
    val color: String,
    val defaultView: String
)

data class ChecklistSection(
    val id: String,
    val name: String,
    val type: String,
    val completionState: String,
    val visible: Boolean,
    val children: List<ChecklistItem>,
    val color: String,
    val backgroundColor: String
)

data class ChecklistItem(
    val id: String,
    val type: String,
    val checked: Boolean,
    val visible: Boolean,
    val enabled: Boolean,
    val label1: String,
    val label2: String,
    val label3: String,
    val mandatory: Boolean,
    val comments: String
)