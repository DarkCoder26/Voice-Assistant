package com.example.actions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class SmsAction : AssistantAction {
    override val intentName: String = "SEND_SMS"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val smsIntent = intent as? AssistantIntent.SendSms
            ?: return ActionResult.Error("Invalid intent payload for SmsAction")

        var recipientNumber = smsIntent.phoneNumber
        var recipientName = smsIntent.contactName

        if (recipientNumber.isBlank() && recipientName.isNotBlank()) {
            if (!contactsManager.hasContactsPermission()) {
                return ActionResult.MissingPermission(
                    permissionKey = Manifest.permission.READ_CONTACTS,
                    explanation = "Contacts permission is required to find $recipientName's phone number."
                )
            }

            val contacts = contactsManager.findContactsByName(recipientName)
            if (contacts.isEmpty()) {
                return ActionResult.Error("I couldn't find '$recipientName' in your contacts.")
            }

            if (contacts.size > 1) {
                return ActionResult.NeedsClarification(
                    question = "Found ${contacts.size} contacts matching '$recipientName'. Which one should I message?",
                    options = contacts.map { it.displayName },
                    contextKey = "CHOOSE_CONTACT_FOR_SMS",
                    originalIntent = smsIntent
                )
            }

            val contact = contacts[0]
            if (contact.phoneNumbers.isEmpty()) {
                return ActionResult.Error("${contact.displayName} has no phone number.")
            }
            recipientName = contact.displayName
            recipientNumber = contact.phoneNumbers[0]
        }

        if (recipientNumber.isBlank()) {
            return ActionResult.Error("Who would you like to send an SMS to?")
        }

        val messageText = smsIntent.message
        if (messageText.isBlank()) {
            return ActionResult.NeedsClarification(
                question = "What message would you like to send to $recipientName?",
                options = listOf("I will reach in 10 minutes", "Call me back", "I'm on my way"),
                contextKey = "PROVIDE_SMS_BODY",
                originalIntent = smsIntent.copy(contactName = recipientName, phoneNumber = recipientNumber)
            )
        }

        return try {
            val sendIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$recipientNumber")
                putExtra("sms_body", messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(sendIntent)

            ActionResult.Success(
                spokenMessage = "Composed SMS for $recipientName.",
                details = "Message: \"$messageText\"",
                visualCardType = VisualCardType.SMS,
                cardData = mapOf(
                    "recipient" to recipientName.ifBlank { recipientNumber },
                    "number" to recipientNumber,
                    "message" to messageText
                )
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to open SMS composer: ${e.message}")
        }
    }
}
