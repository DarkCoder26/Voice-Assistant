package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userQuery: String,
    val assistantResponse: String,
    val intentName: String,
    val actionStatus: String, // "SUCCESS", "FAILED", "CONFIRMATION_PENDING", "CLARIFICATION"
    val timestamp: Long = System.currentTimeMillis()
)
