package com.irys.app.data.repo

import com.irys.app.core.common.AppConstants
import com.irys.app.core.database.dao.MessageDao
import com.irys.app.data.model.toDomain
import com.irys.app.data.model.toEntity
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.model.MessageType
import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.repo.MessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val appSettingsRepository: AppSettingsRepository
) : MessageRepository {

    override fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPendingMessageCount(): Flow<Int> {
        return messageDao.countPendingMessages()
    }

    override suspend fun getMessageById(messageId: String): Message? {
        return messageDao.getMessageById(messageId)?.toDomain()
    }

    override suspend fun saveMessage(message: Message) {
        messageDao.insertMessage(message.toEntity())
    }

    override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status.name)
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }

    override suspend fun createLocalMessage(
        conversationId: String,
        destinationId: String,
        payload: String,
        priority: MessagePriority
    ): Message {
        val localNodeAlias = appSettingsRepository.getNodeAlias()
        val now = System.currentTimeMillis()
        val ttlMillis = AppConstants.DEFAULT_TTL_HOURS * 3600 * 1000L

        val message = Message(
            messageId = UUID.randomUUID().toString(),
            sourceId = localNodeAlias,
            destinationId = destinationId,
            conversationId = conversationId,
            type = MessageType.TEXT,
            payload = payload,
            priority = priority,
            createdAt = now,
            expiresAt = now + ttlMillis,
            hopCount = 0,
            maxHops = AppConstants.DEFAULT_MAX_HOPS,
            status = MessageStatus.QUEUED,
            isOutgoing = true
        )

        messageDao.insertMessage(message.toEntity())
        return message
    }
}
