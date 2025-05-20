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
    val challenge: String = "",
    val challengeAudio: String = "",
    val response: String = "",
    val responseAudio: String = "",
    val mandatory: Boolean = false,
    val suppressAudioChallenge: Boolean = false,
    val suppressAudioResponse: Boolean = false,
    val startNamedTimers: List<String> = emptyList(),
    val stopNamedTimers: List<String> = emptyList(),
    var checked: Boolean = false
)

