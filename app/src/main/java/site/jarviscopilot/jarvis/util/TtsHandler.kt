package site.jarviscopilot.jarvis.util

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.firstOrNull
import site.jarviscopilot.jarvis.data.model.ChecklistData
import site.jarviscopilot.jarvis.data.model.ChecklistItemData
import site.jarviscopilot.jarvis.data.model.ChecklistSectionData

/**
 * TtsHandler is responsible for handling TTS operations in different contexts throughout the app.
 * It follows the app flow and speaks the appropriate content based on the navigation path.
 */
class TtsHandler private constructor(context: Context) {
    private val ttsManager: TtsManager = TtsManager.getInstance(context)
    private val userPreferences: UserPreferences = UserPreferences.getInstance(context)

    /**
     * Speak text if TTS is enabled in user preferences
     */
    private suspend fun speakIfEnabled(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        val isTtsEnabled = userPreferences.ttsEnabledFlow.firstOrNull() ?: userPreferences.isTtsEnabled()
        if (isTtsEnabled && text.isNotBlank()) {
            ttsManager.speak(text, queueMode)
        }
    }

    /**
     * Stop any ongoing speech
     */
    fun stopSpeech() {
        ttsManager.stop()
    }

    /**
     * Handle checklist opening - speak title or titleAudio
     */
    suspend fun handleChecklistOpened(checklist: ChecklistData) {
        val textToSpeak = if (checklist.titleAudio.isNotBlank())
            checklist.titleAudio
        else
            checklist.title

        speakIfEnabled(textToSpeak, TextToSpeech.QUEUE_FLUSH)
    }

    /**
     * Handle section opening - speak sectionAudio or sectionTitle
     */
    suspend fun handleSectionOpened(section: ChecklistSectionData) {
        val textToSpeak = if (section.sectionTitleAudio.isNotBlank())
            section.sectionTitleAudio
        else
            section.sectionTitle

        speakIfEnabled(textToSpeak, TextToSpeech.QUEUE_FLUSH)
    }

    /**
     * Handle list opening - speak listTitleAudio or listTitle
     */
    suspend fun handleListOpened(listTitle: String, listTitleAudio: String) {
        val textToSpeak = if (listTitleAudio.isNotBlank())
            listTitleAudio
        else
            listTitle

        speakIfEnabled(textToSpeak, TextToSpeech.QUEUE_FLUSH)
    }

    /**
     * Handle item activation - This is more complex as it reads items until it gets to a task
     * It should:
     * 1. Read the items in sequence until it finds an active task
     * 2. For a task, read the challengeAudio (or challenge) and responseAudio (or response)
     */
    suspend fun handleItemsAndTask(items: List<ChecklistItemData>, activeItemIndex: Int) {
        // Reset speech queue
        ttsManager.stop()

        // Find the range of items to read
        var endIndex = activeItemIndex

        // If the active item is not a task, find the next task
        if (!items[activeItemIndex].listItemType.equals("task", ignoreCase = true)) {
            for (i in activeItemIndex until items.size) {
                if (items[i].listItemType.equals("task", ignoreCase = true)) {
                    endIndex = i
                    break
                }
            }
        }

        // Read all items from active item to the task
        for (i in activeItemIndex..endIndex) {
            val item = items[i]

            if (item.listItemType.equals("task", ignoreCase = true)) {
                // For tasks, read challenge and response
                // Read challenge
                val challengeText = if (item.challengeAudio.isNotBlank())
                    item.challengeAudio
                else
                    item.challenge

                speakIfEnabled(challengeText)
            } else {
                // For non-task items, just read the content (if available)
                // Using challenge as content since there's no separate content field
                if (item.challenge.isNotBlank()) {
                    speakIfEnabled(item.challenge)
                }
            }
        }
    }

    /**
     * Handle task completion - speak the response
     */
    suspend fun handleTaskCompleted(item: ChecklistItemData) {
        val responseText = if (item.responseAudio.isNotBlank())
            item.responseAudio
        else
            item.response

        speakIfEnabled(responseText)
    }

    companion object {
        @Volatile
        private var INSTANCE: TtsHandler? = null

        fun getInstance(context: Context): TtsHandler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TtsHandler(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
