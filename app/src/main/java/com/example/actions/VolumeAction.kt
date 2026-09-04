package com.example.actions

import android.content.Context
import android.media.AudioManager
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class VolumeAction : AssistantAction {
    override val intentName: String = "SET_VOLUME"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val volumeIntent = intent as? AssistantIntent.SetVolume
            ?: return ActionResult.Error("Invalid intent payload for VolumeAction")

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ActionResult.Error("Audio service unavailable on device.")

        val stream = when (volumeIntent.stream) {
            AssistantIntent.VolumeStream.MUSIC -> AudioManager.STREAM_MUSIC
            AssistantIntent.VolumeStream.RING -> AudioManager.STREAM_RING
            AssistantIntent.VolumeStream.ALARM -> AudioManager.STREAM_ALARM
        }

        val maxVolume = audioManager.getStreamMaxVolume(stream)

        return try {
            when (volumeIntent.type) {
                AssistantIntent.VolumeType.UP -> {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                    val current = audioManager.getStreamVolume(stream)
                    val percent = ((current.toFloat() / maxVolume.toFloat()) * 100).toInt()
                    ActionResult.Success(
                        spokenMessage = "Volume increased to $percent percent.",
                        details = "Media volume: $percent%",
                        visualCardType = VisualCardType.VOLUME,
                        cardData = mapOf("volume" to "$percent%")
                    )
                }
                AssistantIntent.VolumeType.DOWN -> {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                    val current = audioManager.getStreamVolume(stream)
                    val percent = ((current.toFloat() / maxVolume.toFloat()) * 100).toInt()
                    ActionResult.Success(
                        spokenMessage = "Volume decreased to $percent percent.",
                        details = "Media volume: $percent%",
                        visualCardType = VisualCardType.VOLUME,
                        cardData = mapOf("volume" to "$percent%")
                    )
                }
                AssistantIntent.VolumeType.SET_PERCENT -> {
                    val percent = volumeIntent.percent?.coerceIn(0, 100) ?: 50
                    val targetVolume = ((percent.toFloat() / 100f) * maxVolume).toInt().coerceIn(0, maxVolume)
                    audioManager.setStreamVolume(stream, targetVolume, AudioManager.FLAG_SHOW_UI)
                    ActionResult.Success(
                        spokenMessage = "Volume set to $percent percent.",
                        details = "Media volume updated to $percent%",
                        visualCardType = VisualCardType.VOLUME,
                        cardData = mapOf("volume" to "$percent%")
                    )
                }
                AssistantIntent.VolumeType.MUTE -> {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                    ActionResult.Success(
                        spokenMessage = "Phone muted.",
                        details = "Audio muted",
                        visualCardType = VisualCardType.VOLUME,
                        cardData = mapOf("volume" to "0% (Muted)")
                    )
                }
                AssistantIntent.VolumeType.UNMUTE -> {
                    audioManager.adjustStreamVolume(stream, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                    val current = audioManager.getStreamVolume(stream)
                    val percent = ((current.toFloat() / maxVolume.toFloat()) * 100).toInt()
                    ActionResult.Success(
                        spokenMessage = "Phone unmuted. Volume is at $percent percent.",
                        details = "Audio unmuted",
                        visualCardType = VisualCardType.VOLUME,
                        cardData = mapOf("volume" to "$percent%")
                    )
                }
            }
        } catch (e: Exception) {
            ActionResult.Error("Unable to adjust volume: ${e.message}")
        }
    }
}
