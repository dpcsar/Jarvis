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
     * Handle item activation - This method reads items starting from the given index
     * and continues reading until it finds a task, then returns that task's index
     *
     * @param items The list of checklist items
     * @param activeItemIndex The starting index to begin reading from
     * @param queueMode TTS queue mode (QUEUE_FLUSH or QUEUE_ADD)
     * @param completedItems Optional list of completed item indices to skip
     * @return The index of the unchecked task that was found and spoken, or -1 if none found
     */
    suspend fun handleItemsAndTask(
        items: List<ChecklistItemData>,
        activeItemIndex: Int,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        completedItems: List<Int> = emptyList()
    ): Int {
        // Start from the given index
        var currentIndex = activeItemIndex

        // Continue reading items until we find a task
        while (currentIndex >= 0 && currentIndex < items.size) {
            val currentItem = items[currentIndex]

            // If this is a task
            if (currentItem.listItemType.equals("task", ignoreCase = true)) {
                // And it's not completed
                if (!completedItems.contains(currentIndex)) {
                    // Read the task and return its index
                    handleItemDirectly(items, currentIndex, queueMode)
                    return currentIndex
                }
            } else {
                // If it's a label/non-task, read it and continue to the next item
                if (currentItem.challenge.isNotBlank()) {
                    speakIfEnabledAndWait(currentItem.challenge, queueMode)
                }
            }

            // Move to the next item
            currentIndex++
        }

        // No unchecked task was found
        return -1
    }

    /**
     * Handle speaking a specific item directly without searching for the next unchecked task.
     * This function is used when we already know which item to speak.
     *
     * @param items The list of checklist items
     * @param itemIndex The index of the item to speak
     * @param queueMode TTS queue mode (QUEUE_FLUSH or QUEUE_ADD)
     */
    suspend fun handleItemDirectly(
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
            speakIfEnabledAndWait(item.challenge, queueMode)
        }
    }

    /**
     * Find the next unchecked task index in the list
     *
     * @param items The list of checklist items
     * @param startIndex The index to start searching from
     * @param completedItems List of indices of completed items to skip
     * @return The index of the next unchecked task, or -1 if none found
     */
    fun findNextUncheckedTask(
        items: List<ChecklistItemData>,
        startIndex: Int,
        completedItems: List<Int>
    ): Int {
        // Start searching from the specified index
        for (i in startIndex until items.size) {
            // Check if this is an unchecked task
            val item = items.getOrNull(i)
            if (item != null &&
                !completedItems.contains(i) &&
                item.listItemType.equals("TASK", ignoreCase = true)
            ) {
                return i
            }
        }

        // No unchecked task found
        return -1
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
