package com.biprangshu.guardiansathi.Elder.data.AIAssistant

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import com.biprangshu.guardiansathi.BuildConfig


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

class VoiceAssistantViewmodel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(VoiceAssistantState())
    val state: StateFlow<VoiceAssistantState> = _state

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var ttsInitialized = false

    // Gemini API setup
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.1-flash-lite-preview",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    init {
        initializeSpeechRecognizer()
        initializeTextToSpeech()
    }

    private fun initializeSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {
                Log.d("VoiceAssistant", "Ready for speech")
                _state.value = _state.value.copy(isListening = true, error = null)
            }

            override fun onBeginningOfSpeech() {
                Log.d("VoiceAssistant", "speech started")
            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onEndOfSpeech() {
                Log.d("VoiceAssistant","speech ended")
                _state.value = _state.value.copy(isListening = false)

            }

            override fun onError(error: Int) {
                val errorMsg = when(error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission needed"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Unknown error"
                }
                Log.e("VoiceAssistant", "Speech recognition error: $errorMsg")
                _state.value = _state.value.copy(
                    isListening = false,
                    error = errorMsg
                )
            }


            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val transcribedText = matches?.firstOrNull() ?: ""

                Log.d("VoiceAssistant", "Transcribed text: $transcribedText")

                if (transcribedText.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        transcribedText = transcribedText,
                        conversationHistory = _state.value.conversationHistory +
                                Message(transcribedText, isUser = true)
                    )

                    // Send to Gemini
                    sendToGemini(transcribedText)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Optional: Show partial results in real-time
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("hi", "IN")) //hindi

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("VoiceAssistant", "Hindi language not supported, using English")
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                }

                // Slow down speech for elderly users
                textToSpeech?.setSpeechRate(0.8f)
                textToSpeech?.setPitch(1.0f)

                ttsInitialized = true
                Log.d("VoiceAssistant", "TTS initialized successfully")
            } else {
                Log.e("VoiceAssistant", "TTS init failed")
                _state.value = _state.value.copy(error = "Text-to-speech not available")
            }
        }
    }

    fun startListening(language: String = "hi-IN") {
        if (!SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            _state.value = _state.value.copy(error = "Speech recognition not available")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language) // "hi-IN" for Hindi, "as-IN" for Assamese
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceAssistant", "Error starting speech recognition", e)
            _state.value = _state.value.copy(error = "Could not start listening: ${e.message}")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _state.value = _state.value.copy(isListening = false)
    }

    private fun sendToGemini(userMessage: String) {
        viewModelScope.launch {
            try {
                // Build conversation history (last 3 messages only to save tokens)
                val recentHistory = _state.value.conversationHistory.takeLast(6)

                val chat = generativeModel.startChat(
                    history = recentHistory.dropLast(1).map { msg ->
                        content(if (msg.isUser) "user" else "model") {
                            text(msg.text)
                        }
                    }
                )

                // System prompt embedded in first user message if empty history
                val prompt = if (recentHistory.size <= 1) {
                    """You are a helpful voice assistant for elderly users in India. 
                    |Respond in short, simple sentences (max 2-3 sentences). 
                    |Be warm, respectful, and patient. 
                    |Use simple Hindi or English words only.
                    |User asked: $userMessage""".trimMargin()
                } else {
                    userMessage
                }

                val response = chat.sendMessage(prompt)
                val assistantResponse = response.text ?: "I didn't understand that."

                Log.d("VoiceAssistant", "Gemini response: $assistantResponse")

                _state.value = _state.value.copy(
                    assistantResponse = assistantResponse,
                    conversationHistory = _state.value.conversationHistory +
                            Message(assistantResponse, isUser = false)
                )

                // Speak the response
                speak(assistantResponse)
            } catch (e: Exception) {
                Log.e("VoiceAssistant", "Gemini API error", e)
                _state.value = _state.value.copy(error = "Assistant error: ${e.message}")
                speak("Sorry, I couldn't process that.")
            }
        }
    }

    private fun speak(text: String) {
        if (!ttsInitialized) {
            Log.e("VoiceAssistant", "TTS not initialized")
            return
        }

        _state.value = _state.value.copy(isSpeaking = true)

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")

        textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d("VoiceAssistant", "TTS started")
            }

            override fun onDone(utteranceId: String?) {
                Log.d("VoiceAssistant", "TTS done")
                _state.value = _state.value.copy(isSpeaking = false)
            }

            override fun onError(utteranceId: String?) {
                Log.e("VoiceAssistant", "TTS error")
                _state.value = _state.value.copy(isSpeaking = false)
            }
        })
    }

    fun changeLanguage(languageCode: String) {
        val locale = when (languageCode) {
            "hi" -> Locale("hi", "IN")
            "as" -> Locale("as", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            else -> Locale.ENGLISH
        }

        textToSpeech?.language = locale
    }

    fun clearConversation() {
        _state.value = VoiceAssistantState()
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

}