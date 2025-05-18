package site.jarviscopilot.jarvis.model

import com.google.gson.annotations.SerializedName

data class Checklist(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val genre: String = "",
    val publisher: String = "",
    val tags: List<String> = emptyList(),
    val children: List<ChecklistList> = emptyList(),
    val speakMode: String = "check",
    val checklistView: String = "horz",
    val image: String? = null,
    val selectedListIndex: Int = 0,
    @SerializedName("selected") val selected: ChecklistSelection? = null
)

data class ChecklistSelection(
    val listIndex: Int = 0,
    val sectionIndex: Int = 0,
    val itemIndex: Int = 0
)

data class ChecklistList(
    val id: String = "",
    val name: String = "",
    val type: String = "list",
    val completionState: String = "n",
    val visible: Boolean = true,
    val children: List<ChecklistSection> = emptyList(),
    val color: String = "black",
    val defaultView: String = "checklistView"
)

data class ChecklistSection(
    val id: String = "",
    val name: String = "",
    val type: String = "section",
    val completionState: String = "n",
    val visible: Boolean = true,
    val children: List<ChecklistItem> = emptyList(),
    val color: String = "white",
    val backgroundColor: String = "#888888"
)

data class ChecklistItem(
    val id: String = "",
    val type: String = "item",
    val checked: Boolean = false,
    val visible: Boolean = true,
    val enabled: Boolean = true,
    val label1: String = "",
    val label2: String = "",
    val label3: String = "",
    val mandatory: Boolean = false,
    val comments: String = ""
)