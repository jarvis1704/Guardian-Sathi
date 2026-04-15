package com.biprangshu.guardiansathi.Elder.data.AIAssistant

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// ─── State ─────────────────────────────────────────────────────────────────

data class VoiceAssistantState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val transcribedText: String = "",
    val assistantResponse: String = "",
    val conversationHistory: List<Message> = emptyList(),
    val error: String? = null
)

data class Message(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ─── ViewModel ─────────────────────────────────────────────────────────────

@HiltViewModel
class VoiceAssistantViewModel @Inject constructor(
    application: Application,
    private val generativeModel: GenerativeModel
) : androidx.lifecycle.AndroidViewModel(application) {

    private val _state = MutableStateFlow(VoiceAssistantState())
    val state: StateFlow<VoiceAssistantState> = _state

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()

        sendToGemini("")
    }

    // ─── Speech Recognizer ─────────────────────────────────────────────────

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(p0: Bundle?) {
                _state.value = _state.value.copy(isListening = true, error = null)
            }

            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(p0: ByteArray?) {}
            override fun onRmsChanged(p0: Float) {}

            override fun onEndOfSpeech() {
                _state.value = _state.value.copy(isListening = false)
            }

            override fun onError(error: Int) {
                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO                -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT               -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
                    SpeechRecognizer.ERROR_NETWORK              -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT      -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH             -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY      -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER               -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT       -> "No speech input detected"
                    else                                        -> "Unknown error ($error)"
                }
                Log.e(TAG, "STT error: $errorMsg")
                _state.value = _state.value.copy(isListening = false, error = errorMsg)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcribed = matches?.firstOrNull().orEmpty()
                Log.d(TAG, "Transcribed: $transcribed")

                if (transcribed.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        transcribedText = transcribed,
                        conversationHistory = _state.value.conversationHistory +
                                Message(transcribed, isUser = true)
                    )
                    sendToGemini(transcribed)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // ─── TTS ───────────────────────────────────────────────────────────────

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setSpeechRate(0.8f)
                textToSpeech?.setPitch(1.0f)
                ttsInitialized = true
                Log.d(TAG, "TTS initialized")
            } else {
                Log.e(TAG, "TTS init failed")
                _state.value = _state.value.copy(error = "Text-to-speech not available")
            }
        }
    }

    private fun applyTtsLocale(locale: Locale) {
        val result = textToSpeech?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "TTS locale ${locale.language} not supported, falling back to English")
            textToSpeech?.setLanguage(Locale.ENGLISH)
        }
    }

    // ─── Public API ────────────────────────────────────────────────────────

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            _state.value = _state.value.copy(error = "Speech recognition not available")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("en-IN"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting STT", e)
            _state.value = _state.value.copy(error = "Could not start listening: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _state.value = _state.value.copy(isListening = false)
    }



    fun clearConversation() {
        _state.value = VoiceAssistantState()
    }

    // ─── Gemini ────────────────────────────────────────────────────────────

    private fun sendToGemini(userMessage: String) {
        viewModelScope.launch {
            try {
                val recentHistory = _state.value.conversationHistory.takeLast(5)

                val chat = generativeModel.startChat(
                    history = recentHistory.dropLast(1).map { msg ->
                        content(if (msg.isUser) "user" else "model") { text(msg.text) }
                    }
                )

                val prompt = if (recentHistory.size <= 1) {
                    """You are a helpful voice assistant for elderly users in India.
                    |Respond in short, simple sentences (max 2-3 sentences).
                    |Be warm, respectful, and patient.
                    |The user may speak in Hindi only, or English. You will reply preferably in Hindi.
                    |Use simple, common words only. Your response will be fed to a Text-To-speech service so avoid symbols like *,~,/ etc that are purely for bold/italics etc.
                    |User asked: $userMessage
                    |conversation history: ${_state.value.conversationHistory}
                    |""".trimMargin()
                } else {
                    """You are a helpful voice assistant for elderly users in India.
                    |Respond in short, simple sentences (max 2-3 sentences).
                    |Be warm, respectful, and patient.
                    |The user may speak in Hindi only, or English. You will reply preferably in Hindi.
                    |Use simple, common words only. Your response will be fed to a Text-To-speech service so avoid symbols like *,~,/ etc that are purely for bold/italics etc.
                    |If you understood, just reply with 'Namaste, Mai aapki kya sahayata kar sakti hu' in Hindi """.trimMargin()
                }

                val response = chat.sendMessage(prompt)
                val assistantResponse = response.text ?: "I didn't understand that."

                Log.d(TAG, "Gemini response: $assistantResponse")

                _state.value = _state.value.copy(
                    assistantResponse = assistantResponse,
                    conversationHistory = _state.value.conversationHistory +
                            Message(assistantResponse, isUser = false)
                )

                speak(assistantResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API error", e)
                _state.value = _state.value.copy(error = "Assistant error: ${e.message}")
                speak("Sorry, I couldn't process that.")
            }
        }
    }

    private fun speak(text: String) {
        if (!ttsInitialized) return
        _state.value = _state.value.copy(isSpeaking = true)

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        textToSpeech?.setOnUtteranceProgressListener(object :
            android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = false)
            }
            override fun onError(utteranceId: String?) {
                _state.value = _state.value.copy(isSpeaking = false)
            }
        })
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    companion object {
        private const val TAG = "VoiceAssistant"
        private const val UTTERANCE_ID = "utteranceId"
    }
}