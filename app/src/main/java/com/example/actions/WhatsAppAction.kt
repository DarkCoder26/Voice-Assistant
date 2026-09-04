package com.example.actions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import java.net.URLEncoder

class WhatsAppAction : AssistantAction {
    override val intentName: String = "SEND_WHATSAPP_MESSAGE"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val waIntent = intent as? AssistantIntent.SendWhatsApp
            ?: return ActionResult.Error("Invalid intent payload for WhatsAppAction")

        val packageManager = context.packageManager
        val isInstalled = try {
            packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            false
        }

        if (!isInstalled) {
            return ActionResult.Error(
                errorMessage = "WhatsApp isn't installed on this device.",
                technicalReason = "Package com.whatsapp not found in PackageManager"
            )
        }

        var recipientName = waIntent.contactName
        var phoneNumber = waIntent.phoneNumber
        val message = waIntent.message

        if (recipientName.isBlank() && phoneNumber.isBlank()) {
            // Simply open WhatsApp
            val launchIntent = packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return ActionResult.Success(
                    spokenMessage = "Opening WhatsApp.",
                    details = "Launched WhatsApp application.",
                    visualCardType = VisualCardType.WHATSAPP
                )
            } else {
                return ActionResult.Error("Could not launch WhatsApp.")
            }
        }

        if (phoneNumber.isBlank() && recipientName.isNotBlank()) {
            if (!contactsManager.hasContactsPermission()) {
                return ActionResult.MissingPermission(
                    permissionKey = Manifest.permission.READ_CONTACTS,
                    explanation = "Contacts permission is needed to look up $recipientName on WhatsApp."
                )
            }

            val matches = contactsManager.findContactsByName(recipientName)
            if (matches.isEmpty()) {
                return ActionResult.Error("I couldn't find '$recipientName' in your contacts.")
            }

            if (matches.size > 1) {
                return ActionResult.NeedsClarification(
                    question = "I found ${matches.size} contacts named $recipientName. Which one should I message on WhatsApp?",
                    options = matches.map { it.displayName },
                    contextKey = "CHOOSE_WHATSAPP_CONTACT",
                    originalIntent = waIntent
                )
            }

            val contact = matches[0]
            if (contact.phoneNumbers.isEmpty()) {
                return ActionResult.Error("${contact.displayName} has no phone number.")
            }
            recipientName = contact.displayName
            phoneNumber = contact.phoneNumbers[0]
        }

        return try {
            val cleanPhone = phoneNumber.replace(Regex("[^0-9+]"), "")
            val encodedMsg = if (message.isNotBlank()) URLEncoder.encode(message, "UTF-8") else ""
            val uriString = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }

            val intentUrl = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(uriString)
                setPackage("com.whatsapp")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intentUrl)

            val spoken = if (message.isNotBlank()) {
                "Opening WhatsApp chat for $recipientName with your message."
            } else {
                "Opening WhatsApp chat for $recipientName."
            }

            ActionResult.Success(
                spokenMessage = spoken,
                details = "Contact: $recipientName | Message: \"${message.ifBlank { "(None)" }}\"",
                visualCardType = VisualCardType.WHATSAPP,
                cardData = mapOf(
                    "recipient" to recipientName.ifBlank { phoneNumber },
                    "message" to message
                )
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to open WhatsApp chat: ${e.message}")
        }
    }
}
