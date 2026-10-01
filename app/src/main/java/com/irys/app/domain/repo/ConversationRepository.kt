package com.irys.app.domain.repo

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getConversation(conversationId: String): Flow<Conversation?>
    suspend fun getConversationDirect(conversationId: String): Conversation?
    suspend fun getOrCreateConversation(peerId: String, title: String): Conversation
    suspend fun saveConversation(conversation: Conversation)
    suspend fun updateLastMessage(
        conversationId: String,
        lastMessage: String,
        timestamp: Long,
        status: MessageStatus
    )
    suspend fun markAsRead(conversationId: String)
    suspend fun deleteConversation(conversationId: String)
    fun getConversationCount(): Flow<Int>
}
