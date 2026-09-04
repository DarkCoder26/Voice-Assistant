package com.example.actions

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class SettingsAction : AssistantAction {
    override val intentName: String = "OPEN_SETTINGS"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val settingsIntent = intent as? AssistantIntent.SettingsControl
            ?: return ActionResult.Error("Invalid intent payload for SettingsAction")

        val (action, name) = when (settingsIntent.category) {
            AssistantIntent.SettingCategory.WIFI -> Pair(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
            AssistantIntent.SettingCategory.BLUETOOTH -> Pair(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
            AssistantIntent.SettingCategory.SOUND -> Pair(Settings.ACTION_SOUND_SETTINGS, "Sound & Vibration")
            AssistantIntent.SettingCategory.DISPLAY -> Pair(Settings.ACTION_DISPLAY_SETTINGS, "Display Settings")
            AssistantIntent.SettingCategory.ACCESSIBILITY -> Pair(Settings.ACTION_ACCESSIBILITY_SETTINGS, "Accessibility Settings")
            AssistantIntent.SettingCategory.GENERAL -> Pair(Settings.ACTION_SETTINGS, "Device Settings")
        }

        return try {
            val systemIntent = Intent(action).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(systemIntent)
            ActionResult.Success(
                spokenMessage = "Opening $name.",
                details = "Launched system $name.",
                visualCardType = VisualCardType.SYSTEM_SETTING,
                cardData = mapOf("setting" to name)
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to open $name: ${e.message}")
        }
    }
}
