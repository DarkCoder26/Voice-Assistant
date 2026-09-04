package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.ai.GeminiBrain
import com.example.data.local.AuraDatabase
import com.example.data.repository.ConversationRepository
import com.example.permissions.PermissionManager
import com.example.contacts.ContactsManager
import com.example.actions.ActionRegistry

class AuraApplication : Application() {

    lateinit var database: AuraDatabase
        private set

    lateinit var conversationRepository: ConversationRepository
        private set

    lateinit var permissionManager: PermissionManager
        private set

    lateinit var contactsManager: ContactsManager
        private set

    lateinit var geminiBrain: GeminiBrain
        private set

    lateinit var actionRegistry: ActionRegistry
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AuraDatabase.getDatabase(this)
        conversationRepository = ConversationRepository(database.conversationDao())
        permissionManager = PermissionManager(this)
        contactsManager = ContactsManager(this)
        geminiBrain = GeminiBrain()
        actionRegistry = ActionRegistry(this, contactsManager, geminiBrain)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Aura Assistant Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications and status updates from Aura Voice Assistant"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_ALERTS = "aura_voice_channel"

        lateinit var instance: AuraApplication
            private set
    }
}
