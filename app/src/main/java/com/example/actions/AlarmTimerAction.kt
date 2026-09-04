package com.example.actions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class AlarmTimerAction : AssistantAction {
    override val intentName: String = "SET_ALARM_TIMER"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val clockIntent = intent as? AssistantIntent.AlarmTimer
            ?: return ActionResult.Error("Invalid intent payload for AlarmTimerAction")

        return when (clockIntent.type) {
            AssistantIntent.ClockType.TIMER -> {
                val totalSeconds = (clockIntent.minutes ?: 0) * 60 + (clockIntent.seconds ?: 0)
                val duration = if (totalSeconds > 0) totalSeconds else 300 // default 5 mins
                try {
                    val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, duration)
                        putExtra(AlarmClock.EXTRA_MESSAGE, clockIntent.label ?: "Aura Timer")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(timerIntent)
                    val displayTime = if (duration >= 60) "${duration / 60} minute(s)" else "$duration seconds"
                    ActionResult.Success(
                        spokenMessage = "Timer set for $displayTime.",
                        details = "Running timer for $displayTime.",
                        visualCardType = VisualCardType.INFO,
                        cardData = mapOf("type" to "Timer", "duration" to displayTime)
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to set timer: ${e.message}")
                }
            }
            AssistantIntent.ClockType.ALARM -> {
                val hour = clockIntent.hours ?: 7
                val minute = clockIntent.minutes ?: 0
                try {
                    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, clockIntent.label ?: "Aura Alarm")
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(alarmIntent)
                    val formattedMinute = if (minute < 10) "0$minute" else "$minute"
                    val formattedTime = "$hour:$formattedMinute"
                    ActionResult.Success(
                        spokenMessage = "Alarm set for $formattedTime.",
                        details = "Alarm configured for $formattedTime.",
                        visualCardType = VisualCardType.INFO,
                        cardData = mapOf("type" to "Alarm", "time" to formattedTime)
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to set alarm: ${e.message}")
                }
            }
        }
    }
}
