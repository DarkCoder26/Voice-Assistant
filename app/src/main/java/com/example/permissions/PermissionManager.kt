package com.example.permissions

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.services.AuraAccessibilityService
import com.example.services.AuraNotificationListenerService

data class PermissionItem(
    val key: String,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val actionType: ActionType
) {
    enum class ActionType {
        RUNTIME_REQUEST,
        ACCESSIBILITY_SETTINGS,
        NOTIFICATION_LISTENER_SETTINGS,
        OVERLAY_SETTINGS,
        APP_SETTINGS
    }
}

class PermissionManager(private val context: Context) {

    fun isAudioRecordGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun isContactsGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun isPhoneCallGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun isSmsGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

    fun isCameraGranted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    fun isBluetoothGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isSystemAlertWindowGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isPostNotificationGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = ComponentName(context, AuraAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any {
            it.equals(expectedComponentName, ignoreCase = true) ||
            it.contains(context.packageName, ignoreCase = true)
        }
    }

    fun isNotificationListenerEnabled(): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }

    fun getAllPermissionsStatus(): List<PermissionItem> {
        val list = mutableListOf(
            PermissionItem(
                key = Manifest.permission.RECORD_AUDIO,
                title = "Microphone",
                description = "Required for listening to your voice commands and speech recognition.",
                isGranted = isAudioRecordGranted(),
                actionType = PermissionItem.ActionType.RUNTIME_REQUEST
            ),
            PermissionItem(
                key = Manifest.permission.READ_CONTACTS,
                title = "Contacts Access",
                description = "Used to search names and numbers when you ask Aura to call or message someone.",
                isGranted = isContactsGranted(),
                actionType = PermissionItem.ActionType.RUNTIME_REQUEST
            ),
            PermissionItem(
                key = Manifest.permission.CALL_PHONE,
                title = "Phone Calling",
                description = "Enables directly dialing or placing requested phone calls hands-free.",
                isGranted = isPhoneCallGranted(),
                actionType = PermissionItem.ActionType.RUNTIME_REQUEST
            ),
            PermissionItem(
                key = Manifest.permission.SEND_SMS,
                title = "Send SMS",
                description = "Allows preparing and sending SMS messages to contacts upon voice confirmation.",
                isGranted = isSmsGranted(),
                actionType = PermissionItem.ActionType.RUNTIME_REQUEST
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(
                PermissionItem(
                    key = Manifest.permission.POST_NOTIFICATIONS,
                    title = "Notifications",
                    description = "Needed to notify you when timers, alarms, or background tasks complete.",
                    isGranted = isPostNotificationGranted(),
                    actionType = PermissionItem.ActionType.RUNTIME_REQUEST
                )
            )
        }

        list.add(
            PermissionItem(
                key = "SYSTEM_ALERT_WINDOW",
                title = "Display Over Other Apps",
                description = "Enables floating assistant controls and hands-free overlays for system-wide control.",
                isGranted = isSystemAlertWindowGranted(),
                actionType = PermissionItem.ActionType.OVERLAY_SETTINGS
            )
        )

        list.add(
            PermissionItem(
                key = Manifest.permission.CAMERA,
                title = "Camera & Flashlight",
                description = "Allows toggling flashlight and taking hands-free camera photos upon command.",
                isGranted = isCameraGranted(),
                actionType = PermissionItem.ActionType.RUNTIME_REQUEST
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.add(
                PermissionItem(
                    key = Manifest.permission.BLUETOOTH_CONNECT,
                    title = "Bluetooth Control",
                    description = "Enables voice-activated Bluetooth device connectivity and status checks.",
                    isGranted = isBluetoothGranted(),
                    actionType = PermissionItem.ActionType.RUNTIME_REQUEST
                )
            )
        }

        list.add(
            PermissionItem(
                key = "ACCESSIBILITY_SERVICE",
                title = "Accessibility Navigation",
                description = "Permits voice commands to navigate Home, Back, pull down notification shade, or lock the screen legitimately.",
                isGranted = isAccessibilityServiceEnabled(),
                actionType = PermissionItem.ActionType.ACCESSIBILITY_SETTINGS
            )
        )

        list.add(
            PermissionItem(
                key = "NOTIFICATION_LISTENER",
                title = "Notification Reader",
                description = "Allows Aura to read incoming notifications or summarize alerts upon your request.",
                isGranted = isNotificationListenerEnabled(),
                actionType = PermissionItem.ActionType.NOTIFICATION_LISTENER_SETTINGS
            )
        )

        return list
    }

    fun openOverlaySettings() {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings()
        }
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings()
        }
    }

    fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings()
        }
    }

    fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
