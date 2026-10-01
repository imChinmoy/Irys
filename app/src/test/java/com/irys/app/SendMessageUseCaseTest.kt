package com.irys.app

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.model.MessageType
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import com.irys.app.domain.usecases.message.SendMessageUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SendMessageUseCaseTest {

    private class FakeMessageRepository : MessageRepository {
        val messages = mutableListOf<Message>()

        override fun getMessagesForConversation(conversationId: String): Flow<List<Message>> = MutableStateFlow(messages)
        override fun getAllMessages(): Flow<List<Message>> = MutableStateFlow(messages)
        override fun getPendingMessageCount(): Flow<Int> = MutableStateFlow(messages.size)
        override suspend fun getMessageById(messageId: String): Message? = messages.find { it.messageId == messageId }
        override suspend fun saveMessage(message: Message) { messages.add(message) }
        override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {}
        override suspend fun deleteMessage(messageId: String) {}

        override suspend fun createLocalMessage(
            conversationId: String,
            destinationId: String,
            payload: String,
            priority: MessagePriority
        ): Message {
            val message = Message(
                messageId = UUID.randomUUID().toString(),
                sourceId = "LocalNode",
                destinationId = destinationId,
                conversationId = conversationId,
                type = MessageType.TEXT,
                payload = payload,
                priority = priority,
                createdAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000L,
                status = MessageStatus.QUEUED,
                isOutgoing = true
            )
            messages.add(message)
            return message
        }
    }

    private class FakeConversationRepository : ConversationRepository {
        var lastUpdatedMessage: String? = null
        var lastUpdatedStatus: MessageStatus? = null

        override fun getConversations(): Flow<List<Conversation>> = MutableStateFlow(emptyList())
        override fun getConversation(conversationId: String): Flow<Conversation?> = MutableStateFlow(null)
        override suspend fun getConversationDirect(conversationId: String): Conversation? = null
        override suspend fun getOrCreateConversation(peerId: String, title: String): Conversation = throw UnsupportedOperationException()
        override suspend fun saveConversation(conversation: Conversation) {}

        override suspend fun updateLastMessage(
            conversationId: String,
            lastMessage: String,
            timestamp: Long,
            status: MessageStatus
        ) {
            lastUpdatedMessage = lastMessage
            lastUpdatedStatus = status
        }

        override suspend fun markAsRead(conversationId: String) {}
        override suspend fun deleteConversation(conversationId: String) {}
        override fun getConversationCount(): Flow<Int> = MutableStateFlow(1)
    }

    private lateinit var messageRepo: FakeMessageRepository
    private lateinit var convRepo: FakeConversationRepository
    private lateinit var useCase: SendMessageUseCase

    @Before
    fun setUp() {
        messageRepo = FakeMessageRepository()
        convRepo = FakeConversationRepository()
        useCase = SendMessageUseCase(messageRepo, convRepo)
    }

    @Test
    fun sendMessage_createsMessageAndUpdatesConversation() = runTest {
        val result = useCase(
            conversationId = "conv_123",
            destinationId = "peer_xyz",
            payload = "Relay test message",
            priority = MessagePriority.HIGH
        )

        assertNotNull(result.messageId)
        assertEquals("Relay test message", result.payload)
        assertEquals(MessagePriority.HIGH, result.priority)
        assertEquals(MessageStatus.QUEUED, result.status)

        assertEquals("Relay test message", convRepo.lastUpdatedMessage)
        assertEquals(MessageStatus.QUEUED, convRepo.lastUpdatedStatus)
    }

    @Test(expected = IllegalArgumentException::class)
    fun sendMessage_blankPayloadThrowsException() = runTest {
        useCase(
            conversationId = "conv_123",
            destinationId = "peer_xyz",
            payload = "   "
        )
    }
}
