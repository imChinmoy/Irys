package com.irys.app.domain.repo

import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    fun getAllMessages(): Flow<List<Message>>
    fun getPendingMessageCount(): Flow<Int>
    suspend fun getMessageById(messageId: String): Message?
    suspend fun saveMessage(message: Message)
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    suspend fun deleteMessage(messageId: String)
    suspend fun createLocalMessage(
        conversationId: String,
        destinationId: String,
        payload: String,
        priority: MessagePriority = MessagePriority.NORMAL
    ): Message
}
