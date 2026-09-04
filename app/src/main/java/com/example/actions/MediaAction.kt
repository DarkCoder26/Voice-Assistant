package com.example.actions

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.view.KeyEvent
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import java.net.URLEncoder

class MediaAction : AssistantAction {
    override val intentName: String = "MEDIA_CONTROL"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val mediaIntent = intent as? AssistantIntent.MediaControl
            ?: return ActionResult.Error("Invalid intent payload for MediaAction")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

        return when (mediaIntent.action) {
            AssistantIntent.MediaActionType.PLAY, AssistantIntent.MediaActionType.RESUME -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PLAY)
                ActionResult.Success(
                    spokenMessage = "Resuming playback.",
                    details = "Dispatched Media Play signal",
                    visualCardType = VisualCardType.MEDIA,
                    cardData = mapOf("action" to "Play")
                )
            }
            AssistantIntent.MediaActionType.PAUSE -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PAUSE)
                ActionResult.Success(
                    spokenMessage = "Playback paused.",
                    details = "Dispatched Media Pause signal",
                    visualCardType = VisualCardType.MEDIA,
                    cardData = mapOf("action" to "Pause")
                )
            }
            AssistantIntent.MediaActionType.NEXT -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_NEXT)
                ActionResult.Success(
                    spokenMessage = "Skipping to next track.",
                    details = "Dispatched Media Next signal",
                    visualCardType = VisualCardType.MEDIA,
                    cardData = mapOf("action" to "Next Track")
                )
            }
            AssistantIntent.MediaActionType.PREVIOUS -> {
                sendMediaKeyEvent(audioManager, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                ActionResult.Success(
                    spokenMessage = "Returning to previous track.",
                    details = "Dispatched Media Previous signal",
                    visualCardType = VisualCardType.MEDIA,
                    cardData = mapOf("action" to "Previous Track")
                )
            }
            AssistantIntent.MediaActionType.SEARCH_PLAY -> {
                val query = mediaIntent.query ?: "music"
                try {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$encoded")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    if (isPackageInstalled(context, "com.google.android.youtube")) {
                        ytIntent.setPackage("com.google.android.youtube")
                    }
                    context.startActivity(ytIntent)
                    ActionResult.Success(
                        spokenMessage = "Playing $query on YouTube.",
                        details = "Opened YouTube search for \"$query\"",
                        visualCardType = VisualCardType.MEDIA,
                        cardData = mapOf("query" to query, "platform" to "YouTube")
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to search YouTube: ${e.message}")
                }
            }
        }
    }

    private fun sendMediaKeyEvent(audioManager: AudioManager?, keyCode: Int) {
        audioManager?.let {
            val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
            val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)
            it.dispatchMediaKeyEvent(eventDown)
            it.dispatchMediaKeyEvent(eventUp)
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
