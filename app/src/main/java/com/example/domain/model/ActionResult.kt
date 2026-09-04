package com.example.domain.model

sealed class ActionResult {
    data class Success(
        val spokenMessage: String,
        val details: String = "",
        val visualCardType: VisualCardType = VisualCardType.NONE,
        val cardData: Map<String, String> = emptyMap()
    ) : ActionResult()

    data class NeedsConfirmation(
        val question: String,
        val pendingIntent: AssistantIntent,
        val details: String = ""
    ) : ActionResult()

    data class NeedsClarification(
        val question: String,
        val options: List<String>,
        val contextKey: String,
        val originalIntent: AssistantIntent
    ) : ActionResult()

    data class MissingPermission(
        val permissionKey: String,
        val explanation: String
    ) : ActionResult()

    data class Error(
        val errorMessage: String,
        val technicalReason: String? = null
    ) : ActionResult()
}

enum class VisualCardType {
    NONE,
    CALL,
    SMS,
    WHATSAPP,
    VOLUME,
    MEDIA,
    APP_LAUNCH,
    SYSTEM_SETTING,
    INFO,
    AI_ANSWER
}

data class ContactItem(
    val id: String,
    val displayName: String,
    val phoneNumbers: List<String>
)
