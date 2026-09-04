package com.example.actions

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import java.net.URLEncoder

class WebSearchAction : AssistantAction {
    override val intentName: String = "WEB_SEARCH"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val searchIntent = intent as? AssistantIntent.WebSearch
            ?: return ActionResult.Error("Invalid intent payload for WebSearchAction")

        val query = searchIntent.query
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            ActionResult.Success(
                spokenMessage = "Searching Google for $query.",
                details = "Opened Google search results",
                visualCardType = VisualCardType.INFO,
                cardData = mapOf("query" to query)
            )
        } catch (e: Exception) {
            ActionResult.Error("Unable to open web search: ${e.message}")
        }
    }
}
