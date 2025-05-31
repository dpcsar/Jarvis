package site.jarviscopilot.jarvis.util

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.delay
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
     * Speak text if TTS is enabled in user preferences and wait for it to complete
     */
    private suspend fun speakIfEnabledAndWait(
        text: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        val isTtsEnabled =
            userPreferences.ttsEnabledFlow.firstOrNull() ?: userPreferences.isTtsEnabled()
        if (isTtsEnabled && text.isNotBlank()) {
            // Use the new speakAndWait function that properly waits for speech to complete
            ttsManager.speakAndWait(text, queueMode)

            // Add a small pause between speeches for natural flow
            delay(300)
        }
    }

    /**
     * Handle checklist opening - speak title or titleAudio
     */
    suspend fun handleChecklistOpened(checklist: ChecklistData) {
        val textToSpeak = if (checklist.titleAudio.isNotBlank())
            checklist.titleAudio
        else
            checklist.title

        speakIfEnabledAndWait(textToSpeak)
    }

    /**
     * Handle section opening - speak sectionAudio or sectionTitle
     */
    suspend fun handleSectionOpened(section: ChecklistSectionData, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        val textToSpeak = if (section.sectionTitleAudio.isNotBlank())
            section.sectionTitleAudio
        else
            section.sectionTitle

        speakIfEnabledAndWait(textToSpeak, queueMode)
    }

    /**
     * Handle list opening - speak listTitleAudio or listTitle
     */
    suspend fun handleListOpened(listTitle: String, listTitleAudio: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        val textToSpeak = if (listTitleAudio.isNotBlank())
            listTitleAudio
        else
            listTitle

        speakIfEnabledAndWait(textToSpeak, queueMode)
    }

    /**
     * Handle item activation - This is more complex as it reads items until it gets to a task
     * It should:
     * 1. Read the items in sequence until it finds an active task
     * 2. For a task, read the challengeAudio (or challenge) and responseAudio (or response)
     */
    suspend fun handleItemsAndTask(
        items: List<ChecklistItemData>,
        activeItemIndex: Int,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        // Read all labels before the active item
        for (i in 0 until activeItemIndex) {
            val item = items[i]
            if (!item.listItemType.equals(
                    "task",
                    ignoreCase = true
                ) && item.challenge.isNotBlank()
            ) {
                speakIfEnabledAndWait(item.challenge, queueMode)
            }
        }

        // Now read the active item
        if (activeItemIndex < items.size) {
            val activeItem = items[activeItemIndex]

            // Handle challenge part based on suppressAudioChallenge flag
            if (!activeItem.suppressAudioChallenge) {
                val challengeToSpeak = if (activeItem.challengeAudio.isNotBlank()) {
                    activeItem.challengeAudio
                } else {
                    activeItem.challenge
                }
                speakIfEnabledAndWait(challengeToSpeak, queueMode)
            }

            // Handle response part based on suppressAudioResponse flag
            if (!activeItem.suppressAudioResponse &&
                (activeItem.responseAudio.isNotBlank() || activeItem.response.isNotBlank())) {
                val responseToSpeak = if (activeItem.responseAudio.isNotBlank()) {
                    activeItem.responseAudio
                } else {
                    activeItem.response
                }
                speakIfEnabledAndWait(responseToSpeak, TextToSpeech.QUEUE_ADD)
            }
        }
    }

    /**
     * Handle announcement when all tasks are marked complete
     */
    suspend fun handleAllTasksComplete() {
        speakIfEnabledAndWait("All tasks marked complete")
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
