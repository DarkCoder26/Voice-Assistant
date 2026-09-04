package com.example.data.repository

import com.example.data.local.ConversationDao
import com.example.data.local.ConversationEntity
import kotlinx.coroutines.flow.Flow

class ConversationRepository(private val conversationDao: ConversationDao) {
    val conversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    suspend fun recordTurn(
        userQuery: String,
        assistantResponse: String,
        intentName: String,
        actionStatus: String
    ): Long {
        return conversationDao.insertConversation(
            ConversationEntity(
                userQuery = userQuery,
                assistantResponse = assistantResponse,
                intentName = intentName,
                actionStatus = actionStatus
            )
        )
    }

    suspend fun clearHistory() {
        conversationDao.clearAll()
    }

    suspend fun deleteEntry(id: Long) {
        conversationDao.deleteById(id)
    }

    fun search(query: String): Flow<List<ConversationEntity>> {
        return conversationDao.searchConversations(query)
    }
}
