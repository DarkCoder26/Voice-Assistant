package com.example.actions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class CallAction : AssistantAction {
    override val intentName: String = "MAKE_CALL"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val callIntent = intent as? AssistantIntent.MakeCall
            ?: return ActionResult.Error("Invalid intent payload for CallAction")

        // If phone number is directly provided
        if (callIntent.phoneNumber.isNotBlank()) {
            return initiateCall(context, callIntent.phoneNumber, callIntent.contactName.ifBlank { callIntent.phoneNumber })
        }

        // If contact name is provided
        val name = callIntent.contactName
        if (name.isBlank()) {
            return ActionResult.Error("Who would you like to call?")
        }

        if (!contactsManager.hasContactsPermission()) {
            return ActionResult.MissingPermission(
                permissionKey = Manifest.permission.READ_CONTACTS,
                explanation = "Contacts permission is needed to look up '$name'. Would you like to grant it?"
            )
        }

        val matches = contactsManager.findContactsByName(name)
        if (matches.isEmpty()) {
            return ActionResult.Error("I couldn't find '$name' in your contacts.")
        }

        if (matches.size > 1) {
            // Disambiguation for multiple contacts with similar names
            val options = matches.map { it.displayName }
            return ActionResult.NeedsClarification(
                question = "I found ${matches.size} contacts named $name. Which one would you like to call?",
                options = options,
                contextKey = "CHOOSE_CONTACT_FOR_CALL",
                originalIntent = callIntent
            )
        }

        val contact = matches[0]
        if (contact.phoneNumbers.isEmpty()) {
            return ActionResult.Error("${contact.displayName} doesn't have any phone numbers saved.")
        }

        if (contact.phoneNumbers.size > 1) {
            // Multiple phone numbers for this contact
            return ActionResult.NeedsClarification(
                question = "${contact.displayName} has ${contact.phoneNumbers.size} numbers. Which one should I call?",
                options = contact.phoneNumbers,
                contextKey = "CHOOSE_PHONE_NUMBER_FOR_CALL",
                originalIntent = callIntent.copy(contactName = contact.displayName)
            )
        }

        val number = contact.phoneNumbers[0]
        return initiateCall(context, number, contact.displayName)
    }

    private fun initiateCall(context: Context, number: String, displayName: String): ActionResult {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return try {
            val intentAction = if (hasCallPermission) Intent.ACTION_CALL else Intent.ACTION_DIAL
            val dialIntent = Intent(intentAction).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(dialIntent)

            val spoken = if (hasCallPermission) "Calling $displayName..." else "Opening dialer for $displayName..."
            ActionResult.Success(
                spokenMessage = spoken,
                details = "Placing call to $number",
                visualCardType = VisualCardType.CALL,
                cardData = mapOf("name" to displayName, "number" to number)
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to initiate call: ${e.message}")
        }
    }
}
