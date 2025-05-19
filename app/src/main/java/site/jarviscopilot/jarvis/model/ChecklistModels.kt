package site.jarviscopilot.jarvis.model

data class Checklist(
    val name: String = "",
    val nameAudio: String = "",
    val checklistId: String = "",
    val description: String = "",
    val sections: List<ChecklistSection> = emptyList(),
    val contributors: List<Contributor> = emptyList()
)

data class Contributor(
    val name: String = "",
    val username: String = "",
    val nickname: String = ""
)

data class ChecklistSection(
    val type: String = "",
    val name: String = "",
    val nameAudio: String = "",
    val defaultView: String = "checklistView",
    val selectorName: String = "",
    val lists: List<ChecklistList> = emptyList()
)

data class ChecklistList(
    val name: String = "",
    val nameAudio: String = "",
    val selectorName: String = "",
    val items: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val type: String = "item",
    val label1: String = "",
    val label1Audio: String = "",
    val label2: String = "",
    val label2Audio: String = "",
    val mandatory: Boolean = false,
    val suppressAudioLabel1: Boolean = false,
    val suppressAudioLabel2: Boolean = false,
    val startNamedTimers: List<String> = emptyList(),
    val stopNamedTimers: List<String> = emptyList(),
    var checked: Boolean = false
)

