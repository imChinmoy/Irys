package com.irys.app.data.repo

import com.irys.app.core.database.dao.ConversationDao
import com.irys.app.data.model.toDomain
import com.irys.app.data.model.toEntity
import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.repo.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao
) : ConversationRepository {

    override fun getConversations(): Flow<List<Conversation>> {
        return conversationDao.getConversations().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getConversation(conversationId: String): Flow<Conversation?> {
        return conversationDao.getConversationFlow(conversationId).map { it?.toDomain() }
    }

    override suspend fun getConversationDirect(conversationId: String): Conversation? {
        return conversationDao.getConversationDirect(conversationId)?.toDomain()
    }

    override suspend fun getOrCreateConversation(peerId: String, title: String): Conversation {
        val existing = conversationDao.getConversationByPeerId(peerId)
        if (existing != null) {
            return existing.toDomain()
        }

        val newConversation = Conversation(
            conversationId = UUID.randomUUID().toString(),
            peerId = peerId,
            title = title,
            lastMessage = null,
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            lastMessageStatus = null
        )
        conversationDao.insertOrUpdate(newConversation.toEntity())
        return newConversation
    }

    override suspend fun saveConversation(conversation: Conversation) {
        conversationDao.insertOrUpdate(conversation.toEntity())
    }

    override suspend fun updateLastMessage(
        conversationId: String,
        lastMessage: String,
        timestamp: Long,
        status: MessageStatus
    ) {
        conversationDao.updateLastMessage(conversationId, lastMessage, timestamp, status.name)
    }

    override suspend fun markAsRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    override suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteConversation(conversationId)
    }

    override fun getConversationCount(): Flow<Int> {
        return conversationDao.countConversations()
    }
}
