package com.example.actions

import android.content.Context
import com.example.ai.GeminiBrain
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class ActionRegistry(
    private val context: Context,
    private val contactsManager: ContactsManager,
    private val geminiBrain: GeminiBrain
) {

    private val callAction = CallAction()
    private val smsAction = SmsAction()
    private val whatsAppAction = WhatsAppAction()
    private val volumeAction = VolumeAction()
    private val appLaunchAction = AppLaunchAction()
    private val mediaAction = MediaAction()
    private val settingsAction = SettingsAction()
    private val alarmTimerAction = AlarmTimerAction()
    private val accessibilityAction = AccessibilityAction()
    private val notificationAction = NotificationAction()
    private val deviceStateAction = DeviceStateAction()
    private val webSearchAction = WebSearchAction()

    suspend fun dispatch(intent: AssistantIntent): ActionResult {
        return when (intent) {
            is AssistantIntent.MakeCall -> callAction.execute(intent, context, contactsManager)
            is AssistantIntent.SendSms -> smsAction.execute(intent, context, contactsManager)
            is AssistantIntent.SendWhatsApp -> whatsAppAction.execute(intent, context, contactsManager)
            is AssistantIntent.SetVolume -> volumeAction.execute(intent, context, contactsManager)
            is AssistantIntent.LaunchApp -> appLaunchAction.execute(intent, context, contactsManager)
            is AssistantIntent.MediaControl -> mediaAction.execute(intent, context, contactsManager)
            is AssistantIntent.SettingsControl -> settingsAction.execute(intent, context, contactsManager)
            is AssistantIntent.AlarmTimer -> alarmTimerAction.execute(intent, context, contactsManager)
            is AssistantIntent.AccessibilityCommand -> accessibilityAction.execute(intent, context, contactsManager)
            is AssistantIntent.QueryNotifications -> notificationAction.execute(intent, context, contactsManager)
            is AssistantIntent.GetDeviceInfo -> deviceStateAction.execute(intent, context, contactsManager)
            is AssistantIntent.WebSearch -> webSearchAction.execute(intent, context, contactsManager)
            is AssistantIntent.ConversationalAi -> {
                val answer = geminiBrain.askAssistant(intent.prompt)
                ActionResult.Success(
                    spokenMessage = answer,
                    details = "Aura AI Knowledge Response",
                    visualCardType = VisualCardType.AI_ANSWER,
                    cardData = mapOf("prompt" to intent.prompt, "answer" to answer)
                )
            }
            is AssistantIntent.Unknown -> {
                ActionResult.Error("I didn't quite catch that. You can ask me to call someone, play music, or change volume.")
            }
        }
    }
}
