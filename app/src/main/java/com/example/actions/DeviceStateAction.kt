package com.example.actions

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeviceStateAction : AssistantAction {
    override val intentName: String = "GET_DEVICE_INFO"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val infoIntent = intent as? AssistantIntent.GetDeviceInfo
            ?: return ActionResult.Error("Invalid intent payload for DeviceStateAction")

        return when (infoIntent.category) {
            AssistantIntent.InfoCategory.TIME -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                val currentTime = timeFormat.format(Date())
                ActionResult.Success(
                    spokenMessage = "It is currently $currentTime.",
                    details = "Current Time: $currentTime",
                    visualCardType = VisualCardType.INFO,
                    cardData = mapOf("info" to currentTime, "type" to "Time")
                )
            }
            AssistantIntent.InfoCategory.DATE -> {
                val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())
                ActionResult.Success(
                    spokenMessage = "Today is $currentDate.",
                    details = "Date: $currentDate",
                    visualCardType = VisualCardType.INFO,
                    cardData = mapOf("info" to currentDate, "type" to "Date")
                )
            }
            AssistantIntent.InfoCategory.BATTERY -> {
                val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, batteryFilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

                val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                val chargingText = if (isCharging) "and currently charging" else "not charging"
                ActionResult.Success(
                    spokenMessage = "Your battery is at $percent percent $chargingText.",
                    details = "Battery: $percent% ($chargingText)",
                    visualCardType = VisualCardType.INFO,
                    cardData = mapOf("battery" to "$percent%", "status" to chargingText)
                )
            }
        }
    }
}
