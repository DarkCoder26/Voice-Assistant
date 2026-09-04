package com.example.domain.model

sealed class VoiceState(val label: String) {
    object Idle : VoiceState("How can I help you?")
    data class Listening(val amplitude: Float = 0f) : VoiceState("Listening...")
    object Processing : VoiceState("Thinking...")
    data class Executing(val actionSummary: String) : VoiceState(actionSummary)
    data class Completed(val message: String) : VoiceState(message)
    data class Error(val errorDetails: String) : VoiceState(errorDetails)
}
