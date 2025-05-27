@file:Suppress("unused")

package site.jarviscopilot.jarvis.data

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistData(
    val title: String,
    val titleAudio: String,
    val description: String,
    val sections: List<ChecklistSection>
)

@Serializable
data class ChecklistSection(
    val sectionType: String,
    val sectionTitle: String,
    val sectionTitleAudio: String,
    val sectionSelectorName: String,
    val listView: String,
    val lists: List<ChecklistList>
)

@Serializable
data class ChecklistList(
    val listTitle: String,
    val listTitleAudio: String,
    val listSelectorName: String,
    val listItems: List<ChecklistItem>
)

@Serializable
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
