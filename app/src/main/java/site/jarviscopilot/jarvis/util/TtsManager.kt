package site.jarviscopilot.jarvis.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * TtsManager handles all Text-to-Speech operations in the app.
 * It provides an interface to speak text and manages the TTS lifecycle.
 */
class TtsManager private constructor(context: Context) {
    // The TextToSpeech instance
    private var textToSpeech: TextToSpeech? = null

    // Whether TTS is initialized and ready to speak
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // Whether TTS is currently speaking
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Initialize TTS engine
    init {
        initializeTts(context)
    }

    private fun initializeTts(context: Context) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Set language to device default
                val result = textToSpeech?.setLanguage(Locale.getDefault())

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                    _isTtsReady.value = false
                } else {
                    Log.d(TAG, "TTS initialized successfully")
                    _isTtsReady.value = true

                    // Set up progress listener
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }
                    })
                }
            } else {
                Log.e(TAG, "TTS initialization failed with status: $status")
                _isTtsReady.value = false
            }
        }
    }

    /**
     * Speak the given text aloud.
     * @param text The text to be spoken
     * @param queueMode Whether to queue the speech or stop current speech (defaults to queue)
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        if (_isTtsReady.value) {
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech?.speak(text, queueMode, null, utteranceId)
        } else {
            Log.w(TAG, "Attempted to speak while TTS not ready: $text")
        }
    }

    /**
     * Immediately stop TTS speech
     */
    fun stop() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    /**
     * Release TTS resources. Call this when the app is closing or TTS is no longer needed.
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        _isTtsReady.value = false
        _isSpeaking.value = false
    }

    companion object {
        private const val TAG = "TtsManager"

        @Volatile
        private var INSTANCE: TtsManager? = null

        fun getInstance(context: Context): TtsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TtsManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
