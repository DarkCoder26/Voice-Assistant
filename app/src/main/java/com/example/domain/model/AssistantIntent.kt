package com.example.domain.model

sealed class AssistantIntent {
    data class MakeCall(
        val contactName: String = "",
        val phoneNumber: String = ""
    ) : AssistantIntent()

    data class SendSms(
        val contactName: String = "",
        val phoneNumber: String = "",
        val message: String = ""
    ) : AssistantIntent()

    data class SendWhatsApp(
        val contactName: String = "",
        val phoneNumber: String = "",
        val message: String = ""
    ) : AssistantIntent()

    enum class VolumeType { UP, DOWN, SET_PERCENT, MUTE, UNMUTE }
    enum class VolumeStream { MUSIC, RING, ALARM }
    data class SetVolume(
        val type: VolumeType,
        val percent: Int? = null,
        val stream: VolumeStream = VolumeStream.MUSIC
    ) : AssistantIntent()

    data class LaunchApp(
        val appName: String,
        val packageName: String? = null
    ) : AssistantIntent()

    enum class MediaActionType { PLAY, PAUSE, RESUME, NEXT, PREVIOUS, SEARCH_PLAY }
    data class MediaControl(
        val action: MediaActionType,
        val query: String? = null
    ) : AssistantIntent()

    enum class SettingCategory { WIFI, BLUETOOTH, SOUND, DISPLAY, ACCESSIBILITY, GENERAL }
    data class SettingsControl(
        val category: SettingCategory
    ) : AssistantIntent()

    enum class ClockType { ALARM, TIMER }
    data class AlarmTimer(
        val type: ClockType,
        val hours: Int? = null,
        val minutes: Int? = null,
        val seconds: Int? = null,
        val label: String? = null
    ) : AssistantIntent()

    enum class SystemNavAction { HOME, BACK, RECENTS, NOTIFICATIONS, LOCK_SCREEN }
    data class AccessibilityCommand(
        val navAction: SystemNavAction
    ) : AssistantIntent()

    data class QueryNotifications(
        val filter: String = "ALL"
    ) : AssistantIntent()

    enum class InfoCategory { TIME, DATE, BATTERY }
    data class GetDeviceInfo(
        val category: InfoCategory
    ) : AssistantIntent()

    data class WebSearch(
        val query: String
    ) : AssistantIntent()

    data class ConversationalAi(
        val prompt: String
    ) : AssistantIntent()

    data class Unknown(
        val rawInput: String
    ) : AssistantIntent()
}
