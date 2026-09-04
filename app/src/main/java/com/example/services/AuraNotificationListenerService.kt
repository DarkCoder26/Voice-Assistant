package com.example.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

data class NotificationSummary(
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class AuraNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        var instance: AuraNotificationListenerService? = null
            private set

        private val recentNotifications = mutableListOf<NotificationSummary>()

        fun isRunning(): Boolean = instance != null

        fun getLatestNotifications(limit: Int = 5): List<NotificationSummary> {
            synchronized(recentNotifications) {
                return recentNotifications.takeLast(limit).reversed()
            }
        }

        fun clearNotifications() {
            synchronized(recentNotifications) {
                recentNotifications.clear()
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        syncActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { extractSummary(it) }
    }

    override fun onListenerDisconnected() {
        if (instance == this) {
            instance = null
        }
        super.onListenerDisconnected()
    }

    override fun onDestroy() {
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    private fun syncActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            for (sbn in active) {
                extractSummary(sbn)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractSummary(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val summary = NotificationSummary(
            packageName = sbn.packageName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )

        synchronized(recentNotifications) {
            recentNotifications.removeAll { it.packageName == summary.packageName && it.title == summary.title }
            recentNotifications.add(summary)
            if (recentNotifications.size > 20) {
                recentNotifications.removeAt(0)
            }
        }
    }
}
