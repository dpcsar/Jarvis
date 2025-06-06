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
    suspend fun handleSectionOpened(
        section: ChecklistSectionData,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        val textToSpeak = if (section.sectionTitleAudio.isNotBlank())
            section.sectionTitleAudio
        else
            section.sectionTitle

        speakIfEnabledAndWait(textToSpeak, queueMode)
    }

    /**
     * Handle list opening - speak listTitleAudio or listTitle
     */
    suspend fun handleListOpened(
        listTitle: String,
        listTitleAudio: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        val textToSpeak = if (listTitleAudio.isNotBlank())
            listTitleAudio
        else
            listTitle

        speakIfEnabledAndWait(textToSpeak, queueMode)
    }

    /**
     * Handle speaking a specific item directly without searching for the next unchecked task.
     * This function is used when we already know which item to speak.
     *
     * @param items The list of checklist items
     * @param itemIndex The index of the item to speak
     * @param queueMode TTS queue mode (QUEUE_FLUSH or QUEUE_ADD)
     */
    suspend fun handleItem(
        items: List<ChecklistItemData>,
        itemIndex: Int,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        // Make sure the index is valid
        if (itemIndex < 0 || itemIndex >= items.size) return

        val item = items[itemIndex]

        // Handle the item based on its type
        if (item.listItemType.equals("task", ignoreCase = true)) {
            // Speak the task challenge
            if (!item.suppressAudioChallenge) {
                val challengeToSpeak = if (item.challengeAudio.isNotBlank()) {
                    item.challengeAudio
                } else {
                    item.challenge
                }
                speakIfEnabledAndWait(challengeToSpeak, queueMode)
            }

            // Speak the response part if present
            if (!item.suppressAudioResponse &&
                (item.responseAudio.isNotBlank() || item.response.isNotBlank())
            ) {
                val responseToSpeak = if (item.responseAudio.isNotBlank()) {
                    item.responseAudio
                } else {
                    item.response
                }
                speakIfEnabledAndWait(responseToSpeak, TextToSpeech.QUEUE_ADD)
            }
        }
        // For non-task items (labels, notes, etc.)
        else if (item.challenge.isNotBlank()) {
            // Speak the task challenge
            if (!item.suppressAudioChallenge) {
                val challengeToSpeak = if (item.challengeAudio.isNotBlank()) {
                    item.challengeAudio
                } else {
                    item.challenge
                }
                speakIfEnabledAndWait(challengeToSpeak, queueMode)
            }

            // Speak the response part if present
            if (item.listItemType.equals("label", ignoreCase = true))
            // For labels we only speak the challenge
                return
            if (!item.suppressAudioResponse &&
                (item.responseAudio.isNotBlank() || item.response.isNotBlank())
            ) {
                val responseToSpeak = if (item.responseAudio.isNotBlank()) {
                    item.responseAudio
                } else {
                    item.response
                }
                speakIfEnabledAndWait(responseToSpeak, TextToSpeech.QUEUE_ADD)
            }
        }
    }

    /**
     * Handle announcement for any message
     *
     * @param message The message text to announce
     * @param queueMode TTS queue mode (QUEUE_FLUSH or QUEUE_ADD)
     */
    suspend fun handleMessage(
        message: String,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH
    ) {
        speakIfEnabledAndWait(message, queueMode)
    }

    /**
     * Stop any ongoing TTS speech immediately
     */
    fun stopSpeech() {
        ttsManager.stopSpeech()
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
