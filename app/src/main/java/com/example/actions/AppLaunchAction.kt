package com.example.actions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.example.contacts.ContactsManager
import com.example.domain.model.ActionResult
import com.example.domain.model.AssistantIntent
import com.example.domain.model.VisualCardType

class AppLaunchAction : AssistantAction {
    override val intentName: String = "LAUNCH_APP"

    override suspend fun execute(
        intent: AssistantIntent,
        context: Context,
        contactsManager: ContactsManager
    ): ActionResult {
        val launchIntent = intent as? AssistantIntent.LaunchApp
            ?: return ActionResult.Error("Invalid intent payload for AppLaunchAction")

        val appName = launchIntent.appName.trim()
        val packageManager = context.packageManager

        // Special system intent mappings
        when (appName.lowercase()) {
            "camera" -> {
                return try {
                    val camIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(camIntent)
                    ActionResult.Success(
                        spokenMessage = "Opening Camera.",
                        details = "Launched system camera.",
                        visualCardType = VisualCardType.APP_LAUNCH,
                        cardData = mapOf("app" to "Camera")
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to open camera: ${e.message}")
                }
            }
            "settings" -> {
                return try {
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    ActionResult.Success(
                        spokenMessage = "Opening Settings.",
                        details = "Launched Android device settings.",
                        visualCardType = VisualCardType.APP_LAUNCH,
                        cardData = mapOf("app" to "Settings")
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to open settings: ${e.message}")
                }
            }
            "clock", "alarm" -> {
                return try {
                    val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(clockIntent)
                    ActionResult.Success(
                        spokenMessage = "Opening Clock.",
                        details = "Opened Clock and Alarms.",
                        visualCardType = VisualCardType.APP_LAUNCH,
                        cardData = mapOf("app" to "Clock")
                    )
                } catch (e: Exception) {
                    ActionResult.Error("Unable to open clock: ${e.message}")
                }
            }
        }

        // Known package mapping
        val knownPackages = mapOf(
            "youtube" to "com.google.android.youtube",
            "chrome" to "com.android.chrome",
            "whatsapp" to "com.whatsapp",
            "instagram" to "com.instagram.android",
            "spotify" to "com.spotify.music",
            "maps" to "com.google.android.apps.maps",
            "gmail" to "com.google.android.gm",
            "photos" to "com.google.android.apps.photos",
            "calculator" to "com.google.android.calculator"
        )

        val targetPackage = launchIntent.packageName
            ?: knownPackages[appName.lowercase()]

        if (targetPackage != null) {
            val appIntent = packageManager.getLaunchIntentForPackage(targetPackage)
            if (appIntent != null) {
                appIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(appIntent)
                return ActionResult.Success(
                    spokenMessage = "Opening $appName.",
                    details = "Launched $appName ($targetPackage).",
                    visualCardType = VisualCardType.APP_LAUNCH,
                    cardData = mapOf("app" to appName, "package" to targetPackage)
                )
            }
        }

        // Search installed launcher activities
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = packageManager.queryIntentActivities(mainIntent, 0)
            val matched = apps.firstOrNull {
                val label = it.loadLabel(packageManager).toString()
                label.contains(appName, ignoreCase = true) || appName.contains(label, ignoreCase = true)
            }

            if (matched != null) {
                val pkg = matched.activityInfo.packageName
                val startIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (startIntent != null) {
                    startIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(startIntent)
                    val label = matched.loadLabel(packageManager).toString()
                    return ActionResult.Success(
                        spokenMessage = "Opening $label.",
                        details = "Launched $label ($pkg).",
                        visualCardType = VisualCardType.APP_LAUNCH,
                        cardData = mapOf("app" to label, "package" to pkg)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ActionResult.Error(
            errorMessage = "I couldn't find '$appName' installed on this device.",
            technicalReason = "No matching launcher package found for '$appName'"
        )
    }
}
