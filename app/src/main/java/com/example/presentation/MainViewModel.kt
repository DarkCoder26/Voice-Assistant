package com.example.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.AuraApplication
import com.example.ai.IntentParser
import com.example.data.local.ConversationEntity
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VoiceState
import com.example.permissions.PermissionItem
import com.example.voice.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssistantUiState(
    val voiceState: VoiceState = VoiceState.Idle,
    val audioLevel: Float = 0f,
    val currentTranscript: String = "",
    val lastResult: ActionResult? = null,
    val pendingClarification: ActionResult.NeedsClarification? = null,
    val pendingConfirmation: ActionResult.NeedsConfirmation? = null,
    val isMicListening: Boolean = false,
    val speechSpeed: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val isVoiceMuted: Boolean = false,
    val permissions: List<PermissionItem> = emptyList(),
    val isOnlineAiAvailable: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as AuraApplication
    private val conversationRepo = app.conversationRepository
    private val actionRegistry = app.actionRegistry
    val permissionManager = app.permissionManager

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val _history = MutableStateFlow<List<ConversationEntity>>(emptyList())
    val history: StateFlow<List<ConversationEntity>> = _history.asStateFlow()

    private var executionJob: Job? = null

    val voiceManager: VoiceManager = VoiceManager(
        context = application,
        onSpeechTextResult = { recognizedText ->
            processRecognizedSpeech(recognizedText)
        },
        onAudioLevelChanged = { level ->
            _uiState.value = _uiState.value.copy(audioLevel = level)
        },
        onListeningStateChanged = { listening ->
            _uiState.value = _uiState.value.copy(
                isMicListening = listening,
                voiceState = if (listening) VoiceState.Listening(_uiState.value.audioLevel) else VoiceState.Idle
            )
        },
        onErrorReceived = { errorMsg ->
            _uiState.value = _uiState.value.copy(
                isMicListening = false,
                voiceState = VoiceState.Error(errorMsg)
            )
        },
        onPartialSpeechResult = { partial ->
            _uiState.value = _uiState.value.copy(currentTranscript = partial)
        }
    )

    init {
        refreshPermissions()
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            conversationRepo.conversations.collect { list ->
                _history.value = list
            }
        }
    }

    fun refreshPermissions() {
        _uiState.value = _uiState.value.copy(
            permissions = permissionManager.getAllPermissionsStatus()
        )
    }

    fun onMicClicked() {
        if (_uiState.value.isMicListening) {
            voiceManager.stopListening()
            _uiState.value = _uiState.value.copy(
                isMicListening = false,
                voiceState = VoiceState.Idle
            )
        } else {
            // Check microphone permission
            if (!permissionManager.isAudioRecordGranted()) {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Error("Microphone permission required. Please grant access in settings.")
                )
                return
            }
            voiceManager.startListening()
            _uiState.value = _uiState.value.copy(
                isMicListening = true,
                currentTranscript = "",
                voiceState = VoiceState.Listening(0f)
            )
        }
    }

    fun submitTextQuery(text: String) {
        if (text.isBlank()) return
        voiceManager.stopSpeaking()
        processRecognizedSpeech(text)
    }

    private fun processRecognizedSpeech(userInput: String) {
        val trimmed = userInput.trim()
        _uiState.value = _uiState.value.copy(
            currentTranscript = trimmed,
            voiceState = VoiceState.Processing,
            isMicListening = false
        )

        executionJob?.cancel()
        executionJob = viewModelScope.launch {
            // Check if there is an active clarification waiting for context
            val clarification = _uiState.value.pendingClarification
            if (clarification != null) {
                handleClarificationResponse(trimmed, clarification)
                return@launch
            }

            // Normal intent parsing
            val intent = IntentParser.parse(trimmed)
            executeAssistantIntent(intent, trimmed)
        }
    }

    private suspend fun executeAssistantIntent(intent: AssistantIntent, originalQuery: String) {
        _uiState.value = _uiState.value.copy(
            voiceState = VoiceState.Executing("Executing action...")
        )

        val result = actionRegistry.dispatch(intent)

        when (result) {
            is ActionResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Completed(result.spokenMessage),
                    lastResult = result,
                    pendingClarification = null,
                    pendingConfirmation = null
                )
                voiceManager.speak(result.spokenMessage)

                conversationRepo.recordTurn(
                    userQuery = originalQuery,
                    assistantResponse = result.spokenMessage,
                    intentName = intent.javaClass.simpleName,
                    actionStatus = "SUCCESS"
                )
            }
            is ActionResult.NeedsClarification -> {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Completed(result.question),
                    lastResult = result,
                    pendingClarification = result
                )
                voiceManager.speak(result.question)
            }
            is ActionResult.NeedsConfirmation -> {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Completed(result.question),
                    lastResult = result,
                    pendingConfirmation = result
                )
                voiceManager.speak(result.question)
            }
            is ActionResult.MissingPermission -> {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Error(result.explanation),
                    lastResult = result
                )
                voiceManager.speak(result.explanation)
            }
            is ActionResult.Error -> {
                _uiState.value = _uiState.value.copy(
                    voiceState = VoiceState.Error(result.errorMessage),
                    lastResult = result,
                    pendingClarification = null,
                    pendingConfirmation = null
                )
                voiceManager.speak(result.errorMessage)

                conversationRepo.recordTurn(
                    userQuery = originalQuery,
                    assistantResponse = result.errorMessage,
                    intentName = intent.javaClass.simpleName,
                    actionStatus = "FAILED"
                )
            }
        }
    }

    private suspend fun handleClarificationResponse(
        response: String,
        clarification: ActionResult.NeedsClarification
    ) {
        _uiState.value = _uiState.value.copy(pendingClarification = null)

        when (clarification.contextKey) {
            "CHOOSE_CONTACT_FOR_CALL" -> {
                // User picked a name from multiple matches
                val matched = clarification.options.firstOrNull {
                    it.contains(response, ignoreCase = true) || response.contains(it, ignoreCase = true)
                } ?: clarification.options.firstOrNull() ?: response

                executeAssistantIntent(
                    AssistantIntent.MakeCall(contactName = matched),
                    "Call $matched"
                )
            }
            "CHOOSE_PHONE_NUMBER_FOR_CALL" -> {
                // User picked or specified number
                val number = clarification.options.firstOrNull { response.contains(it) }
                    ?: if (response.contains("second") || response.contains("dusra")) {
                        clarification.options.getOrNull(1) ?: clarification.options[0]
                    } else {
                        clarification.options[0]
                    }

                val orig = clarification.originalIntent as? AssistantIntent.MakeCall
                executeAssistantIntent(
                    AssistantIntent.MakeCall(
                        contactName = orig?.contactName ?: "",
                        phoneNumber = number
                    ),
                    "Call $number"
                )
            }
            "PROVIDE_SMS_BODY" -> {
                val orig = clarification.originalIntent as? AssistantIntent.SendSms
                if (orig != null) {
                    executeAssistantIntent(
                        orig.copy(message = response),
                        "Send SMS to ${orig.contactName}: $response"
                    )
                }
            }
            "CHOOSE_CONTACT_FOR_SMS" -> {
                val matched = clarification.options.firstOrNull {
                    it.contains(response, ignoreCase = true) || response.contains(it, ignoreCase = true)
                } ?: clarification.options[0]
                val orig = clarification.originalIntent as? AssistantIntent.SendSms
                executeAssistantIntent(
                    orig?.copy(contactName = matched) ?: AssistantIntent.SendSms(contactName = matched),
                    "SMS to $matched"
                )
            }
            "CHOOSE_WHATSAPP_CONTACT" -> {
                val matched = clarification.options.firstOrNull {
                    it.contains(response, ignoreCase = true) || response.contains(it, ignoreCase = true)
                } ?: clarification.options[0]
                val orig = clarification.originalIntent as? AssistantIntent.SendWhatsApp
                executeAssistantIntent(
                    orig?.copy(contactName = matched) ?: AssistantIntent.SendWhatsApp(contactName = matched),
                    "WhatsApp to $matched"
                )
            }
            else -> {
                executeAssistantIntent(IntentParser.parse(response), response)
            }
        }
    }

    fun selectClarificationOption(option: String) {
        val clarification = _uiState.value.pendingClarification ?: return
        viewModelScope.launch {
            handleClarificationResponse(option, clarification)
        }
    }

    fun confirmPendingAction() {
        val confirmation = _uiState.value.pendingConfirmation ?: return
        _uiState.value = _uiState.value.copy(pendingConfirmation = null)
        viewModelScope.launch {
            executeAssistantIntent(confirmation.pendingIntent, "Confirmed Action")
        }
    }

    fun cancelPendingAction() {
        _uiState.value = _uiState.value.copy(
            pendingConfirmation = null,
            voiceState = VoiceState.Idle
        )
        voiceManager.speak("Action cancelled.")
    }

    fun setSpeechSpeed(speed: Float) {
        voiceManager.speechSpeed = speed
        _uiState.value = _uiState.value.copy(speechSpeed = speed)
    }

    fun setSpeechPitch(pitch: Float) {
        voiceManager.speechPitch = pitch
        _uiState.value = _uiState.value.copy(speechPitch = pitch)
    }

    fun toggleVoiceMute() {
        val newMute = !_uiState.value.isVoiceMuted
        voiceManager.isVoiceMuted = newMute
        _uiState.value = _uiState.value.copy(isVoiceMuted = newMute)
    }

    fun clearHistory() {
        viewModelScope.launch {
            conversationRepo.clearHistory()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            conversationRepo.deleteEntry(id)
        }
    }

    override fun onCleared() {
        voiceManager.destroy()
        super.onCleared()
    }
}
