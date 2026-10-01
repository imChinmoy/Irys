package com.irys.app

import com.irys.app.core.database.dao.MessageDao
import com.irys.app.core.database.entity.MessageEntity
import com.irys.app.data.repo.MessageRepositoryImpl
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.repo.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageRepositoryTest {

    private class FakeMessageDao : MessageDao {
        val messages = mutableListOf<MessageEntity>()
        val flow = MutableStateFlow<List<MessageEntity>>(emptyList())

        private fun notifyChange() {
            flow.value = messages.toList()
        }

        override suspend fun insertMessage(message: MessageEntity) {
            messages.removeAll { it.messageId == message.messageId }
            messages.add(message)
            notifyChange()
        }

        override suspend fun insertMessages(messages: List<MessageEntity>) {
            this.messages.addAll(messages)
            notifyChange()
        }

        override suspend fun updateMessage(message: MessageEntity) {
            insertMessage(message)
        }

        override suspend fun updateMessageStatus(messageId: String, status: String) {
            val index = messages.indexOfFirst { it.messageId == messageId }
            if (index != -1) {
                messages[index] = messages[index].copy(status = status)
                notifyChange()
            }
        }

        override suspend fun getMessageById(messageId: String): MessageEntity? {
            return messages.find { it.messageId == messageId }
        }

        override fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
            return flow.map { list -> list.filter { it.conversationId == conversationId } }
        }

        override fun getAllMessages(): Flow<List<MessageEntity>> = flow

        override fun countPendingMessages(): Flow<Int> {
            return flow.map { list ->
                list.count { it.status in listOf("CREATED", "QUEUED", "STORED") && it.isOutgoing }
            }
        }

        override suspend fun deleteMessage(messageId: String) {
            messages.removeAll { it.messageId == messageId }
            notifyChange()
        }

        override suspend fun deleteMessagesForConversation(conversationId: String) {
            messages.removeAll { it.conversationId == conversationId }
            notifyChange()
        }
    }

    private class FakeAppSettingsRepository : AppSettingsRepository {
        override fun isOnboardingCompleted(): Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun getNodeAlias(): String = "LocalNode1"
        override suspend fun setNodeAlias(alias: String) {}
    }

    private lateinit var dao: FakeMessageDao
    private lateinit var repository: MessageRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeMessageDao()
        repository = MessageRepositoryImpl(dao, FakeAppSettingsRepository())
    }

    @Test
    fun createLocalMessage_persistsMessageWithDefaults() = runTest {
        val message = repository.createLocalMessage(
            conversationId = "conv_1",
            destinationId = "peer_b",
            payload = "Hello mesh network",
            priority = MessagePriority.NORMAL
        )

        assertNotNull(message.messageId)
        assertEquals("LocalNode1", message.sourceId)
        assertEquals("peer_b", message.destinationId)
        assertEquals("conv_1", message.conversationId)
        assertEquals("Hello mesh network", message.payload)
        assertEquals(MessageStatus.QUEUED, message.status)
        assertTrue(message.isOutgoing)
        assertTrue(message.expiresAt > message.createdAt)

        val retrieved = repository.getMessageById(message.messageId)
        assertNotNull(retrieved)
        assertEquals(message.messageId, retrieved?.messageId)
    }

    @Test
    fun getMessagesForConversation_observesMessagesViaFlow() = runTest {
        repository.createLocalMessage("conv_1", "peer_b", "First message")
        repository.createLocalMessage("conv_1", "peer_b", "Second message")
        repository.createLocalMessage("conv_2", "peer_c", "Different conversation message")

        val messages = repository.getMessagesForConversation("conv_1").first()
        assertEquals(2, messages.size)
        assertEquals("First message", messages[0].payload)
        assertEquals("Second message", messages[1].payload)
    }

    @Test
    fun updateMessageStatus_updatesStateInFlow() = runTest {
        val message = repository.createLocalMessage("conv_1", "peer_b", "Status check")
        assertEquals(MessageStatus.QUEUED, message.status)

        repository.updateMessageStatus(message.messageId, MessageStatus.DELIVERED)

        val updated = repository.getMessageById(message.messageId)
        assertEquals(MessageStatus.DELIVERED, updated?.status)

        val flowMessages = repository.getMessagesForConversation("conv_1").first()
        assertEquals(MessageStatus.DELIVERED, flowMessages[0].status)
    }

    @Test
    fun countPendingMessages_countsOutgoingQueuedMessages() = runTest {
        val m1 = repository.createLocalMessage("conv_1", "peer_b", "M1")
        val m2 = repository.createLocalMessage("conv_1", "peer_b", "M2")

        val countInitial = repository.getPendingMessageCount().first()
        assertEquals(2, countInitial)

        repository.updateMessageStatus(m1.messageId, MessageStatus.DELIVERED)
        val countAfterDelivery = repository.getPendingMessageCount().first()
        assertEquals(1, countAfterDelivery)
    }
}
