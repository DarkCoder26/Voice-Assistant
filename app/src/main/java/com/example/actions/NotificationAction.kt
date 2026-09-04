package com.example.actions

import android.content.Context
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import com.example.services.AuraNotificationListenerService

class NotificationAction : AssistantAction {
    override val intentName: String = "QUERY_NOTIFICATIONS"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        if (!AuraNotificationListenerService.isRunning()) {
            return ActionResult.MissingPermission(
                permissionKey = "NOTIFICATION_LISTENER",
                explanation = "Notification Listener access is required to read notifications aloud. Please grant access in Settings."
            )
        }

        val notifications = AuraNotificationListenerService.getLatestNotifications(limit = 4)
        if (notifications.isEmpty()) {
            return ActionResult.Success(
                spokenMessage = "You have no active unread notifications right now.",
                details = "Notification tray is clear.",
                visualCardType = VisualCardType.INFO
            )
        }

        val summaryText = notifications.joinToString(". ") { "${it.title}: ${it.text}" }
        val spoken = "You have ${notifications.size} recent notifications. $summaryText"

        return ActionResult.Success(
            spokenMessage = spoken,
            details = "${notifications.size} active notifications found.",
            visualCardType = VisualCardType.INFO,
            cardData = mapOf(
                "count" to "${notifications.size}",
                "summary" to summaryText
            )
        )
    }
}
