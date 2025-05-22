package site.jarviscopilot.jarvis.data.model

data class Checklist(
    val title: String,
    val titleAudio: String? = null,
    val description: String? = null,
    val sections: List<ChecklistSection>,
    val contributors: List<Contributor>? = null
)

data class Contributor(
    val listTitle: String,
    val username: String,
    val nickname: String? = null
)

data class ChecklistSection(
    val type: String,
    val sectionTitle: String,
    val sectionTitleAudio: String? = null,
    val sectionSelectorName: String? = null,
    val defaultView: String? = null,
    val lists: List<ChecklistList>
)

data class ChecklistList(
    val listTitle: String,
    val listTitleAudio: String? = null,
    val listSelectorName: String? = null,
    val items: List<CheckListTask>
)

data class CheckListTask(
    val type: String = "task",
    val challenge: String,
    val challengeAudio: String? = null,
    val response: String,
    val responseAudio: String? = null,
    val mandatory: Boolean = false,
    val suppressAudioChallenge: Boolean = false,
    val suppressAudioResponse: Boolean = false
)
