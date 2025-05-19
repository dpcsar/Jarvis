package site.jarviscopilot.jarvis.model

import com.google.gson.annotations.SerializedName

data class Checklist(
    val name: String = "",
    @SerializedName("nameAudio") val nameAudio: String = "",
    @SerializedName("checklistId") val checklistId: String = "",
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
    @SerializedName("nameAudio") val nameAudio: String = "",
    @SerializedName("defaultView") val defaultView: String = "checklistView",
    @SerializedName("selectorName") val selectorName: String = "",
    val lists: List<ChecklistList> = emptyList()
)

data class ChecklistList(
    val name: String = "",
    @SerializedName("nameAudio") val nameAudio: String = "",
    @SerializedName("selectorName") val selectorName: String = "",
    val items: List<ChecklistItem> = emptyList()
)

data class ChecklistItem(
    val type: String = "item",
    val label1: String = "",
    @SerializedName("label1Audio") val label1Audio: String = "",
    val label2: String = "",
    @SerializedName("label2Audio") val label2Audio: String = "",
    val mandatory: Boolean = false,
    @SerializedName("suppressAudioLabel1") val suppressAudioLabel1: Boolean = false,
    @SerializedName("suppressAudioLabel2") val suppressAudioLabel2: Boolean = false,
    @SerializedName("startNamedTimers") val startNamedTimers: List<String> = emptyList(),
    @SerializedName("stopNamedTimers") val stopNamedTimers: List<String> = emptyList(),
    var checked: Boolean = false
)

