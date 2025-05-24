package site.jarviscopilot.jarvis.data

data class Checklist(
    val title: String,
    val titleAudio: String,
    val description: String,
    val sections: List<ChecklistSection>
)

data class ChecklistSection(
    val sectionType: String,
    val sectionTitle: String,
    val sectionTitleAudio: String,
    val sectionSelectorName: String,
    val defaultView: String,
    val lists: List<ChecklistList>
)

data class ChecklistList(
    val listTitle: String,
    val listTitleAudio: String,
    val listSelectorName: String,
    val listItems: List<ChecklistItem>
)

data class ChecklistItem(
    val listItemType: String,
    val challenge: String,
    val challengeAudio: String,
    val response: String,
    val responseAudio: String,
    val mandatory: Boolean,
    val suppressAudioChallenge: Boolean,
    val suppressAudioResponse: Boolean
)
