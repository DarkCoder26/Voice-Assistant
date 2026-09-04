package com.example.actions

import android.content.Context
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import com.example.services.AuraAccessibilityService

class AccessibilityAction : AssistantAction {
    override val intentName: String = "ACCESSIBILITY_NAV"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val navIntent = intent as? AssistantIntent.AccessibilityCommand
            ?: return ActionResult.Error("Invalid intent payload for AccessibilityAction")

        val service = AuraAccessibilityService.instance
        if (service == null) {
            return ActionResult.MissingPermission(
                permissionKey = "ACCESSIBILITY_SERVICE",
                explanation = "Aura Accessibility Service is required to perform hands-free screen navigation. Please enable it in Settings."
            )
        }

        val success = when (navIntent.navAction) {
            AssistantIntent.SystemNavAction.HOME -> service.triggerHome()
            AssistantIntent.SystemNavAction.BACK -> service.triggerBack()
            AssistantIntent.SystemNavAction.RECENTS -> service.triggerRecents()
            AssistantIntent.SystemNavAction.NOTIFICATIONS -> service.triggerNotifications()
            AssistantIntent.SystemNavAction.LOCK_SCREEN -> service.triggerLockScreen()
        }

        val actionName = when (navIntent.navAction) {
            AssistantIntent.SystemNavAction.HOME -> "Going Home."
            AssistantIntent.SystemNavAction.BACK -> "Navigating Back."
            AssistantIntent.SystemNavAction.RECENTS -> "Opening Recents."
            AssistantIntent.SystemNavAction.NOTIFICATIONS -> "Opening Notification Panel."
            AssistantIntent.SystemNavAction.LOCK_SCREEN -> "Locking screen."
        }

        return if (success) {
            ActionResult.Success(
                spokenMessage = actionName,
                details = "Executed accessibility gesture",
                visualCardType = VisualCardType.SYSTEM_SETTING,
                cardData = mapOf("action" to actionName)
            )
        } else {
            ActionResult.Error("Could not perform $actionName via Accessibility Service.")
        }
    }
}
