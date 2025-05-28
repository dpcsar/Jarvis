package site.jarviscopilot.jarvis.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistData(
    val title: String,
    val titleAudio: String,
    val description: String,
    val sections: List<ChecklistSectionData>
)

@Serializable
data class ChecklistSectionData(
    val sectionType: String,
    val sectionTitle: String,
    val sectionTitleAudio: String,
    val sectionSelectorName: String,
    val listView: String,
    val lists: List<ChecklistListData>
)

@Serializable
data class ChecklistListData(
    val listTitle: String,
    val listTitleAudio: String,
    val listSelectorName: String,
    val listItems: List<ChecklistItemData>
)

@Serializable
data class ChecklistItemData(
    val listItemType: String = "",
    val challenge: String = "",
    val challengeAudio: String = "",
    val response: String = "",
    val responseAudio: String = "",
    val isRequired: Boolean = false,
    val suppressAudioChallenge: Boolean = false,
    val suppressAudioResponse: Boolean = false,
)

@Serializable
data class ChecklistStateData(
    val checklistName: String,
    val currentSectionIndex: Int = 0,
    val currentListIndices: MutableMap<Int, Int> = mutableMapOf(),
    val completedItems: MutableMap<String, MutableList<String>> = mutableMapOf()
)

data class ChecklistInfoData(
    val id: String,
    val name: String,
    val description: String,
    val filename: String,
    val isExample: Boolean
)
