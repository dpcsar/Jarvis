package site.jarviscopilot.jarvis.data.model

data class Checklist(
    val title: String,
    val aircraft: String,
    val sections: List<ChecklistSection>
)

data class ChecklistSection(
    val title: String,
    val items: List<ChecklistItem>
)

sealed class ChecklistItem {
    abstract val type: String
    abstract val text: String
}

data class CheckboxItem(
    override val text: String,
    val description: String? = null,
    val mandatory: Boolean = false,
    val isCompleted: Boolean = false
) : ChecklistItem() {
    override val type: String = "checkbox"
}

data class WarningItem(
    override val text: String
) : ChecklistItem() {
    override val type: String = "warning"
}

data class NoteItem(
    override val text: String
) : ChecklistItem() {
    override val type: String = "note"
}

data class InformationItem(
    override val text: String
) : ChecklistItem() {
    override val type: String = "information"
}

data class YesNoItem(
    override val text: String,
    val description: String? = null,
    val mandatory: Boolean = false,
    val isCompleted: Boolean = false
) : ChecklistItem() {
    override val type: String = "yesno"
}

data class LabelItem(
    override val text: String
) : ChecklistItem() {
    override val type: String = "label"
}
