package com.irys.app

import com.irys.app.core.database.dao.ConversationDao
import com.irys.app.core.database.entity.ConversationEntity
import com.irys.app.data.repo.ConversationRepositoryImpl
import com.irys.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ConversationRepositoryTest {

    private class FakeConversationDao : ConversationDao {
        val conversations = mutableListOf<ConversationEntity>()
        val flow = MutableStateFlow<List<ConversationEntity>>(emptyList())

        private fun notifyChange() {
            flow.value = conversations.toList()
        }

        override suspend fun insertOrUpdate(conversation: ConversationEntity) {
            conversations.removeAll { it.conversationId == conversation.conversationId }
            conversations.add(conversation)
            notifyChange()
        }

        override fun getConversations(): Flow<List<ConversationEntity>> = flow

        override fun getConversationFlow(conversationId: String): Flow<ConversationEntity?> {
            return flow.map { list -> list.find { it.conversationId == conversationId } }
        }

        override suspend fun getConversationDirect(conversationId: String): ConversationEntity? {
            return conversations.find { it.conversationId == conversationId }
        }

        override suspend fun getConversationByPeerId(peerId: String): ConversationEntity? {
            return conversations.find { it.peerId == peerId }
        }

        override suspend fun updateLastMessage(
            conversationId: String,
            lastMessage: String,
            timestamp: Long,
            status: String
        ) {
            val index = conversations.indexOfFirst { it.conversationId == conversationId }
            if (index != -1) {
                conversations[index] = conversations[index].copy(
                    lastMessage = lastMessage,
                    lastMessageTimestamp = timestamp,
                    lastMessageStatus = status
                )
                notifyChange()
            }
        }

        override suspend fun markAsRead(conversationId: String) {
            val index = conversations.indexOfFirst { it.conversationId == conversationId }
            if (index != -1) {
                conversations[index] = conversations[index].copy(unreadCount = 0)
                notifyChange()
            }
        }

        override suspend fun deleteConversation(conversationId: String) {
            conversations.removeAll { it.conversationId == conversationId }
            notifyChange()
        }

        override fun countConversations(): Flow<Int> = flow.map { it.size }
    }

    private lateinit var dao: FakeConversationDao
    private lateinit var repository: ConversationRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeConversationDao()
        repository = ConversationRepositoryImpl(dao)
    }

    @Test
    fun getOrCreateConversation_createsWhenNotExisting() = runTest {
        val conv = repository.getOrCreateConversation(peerId = "peer_alpha", title = "Alpha Node")

        assertNotNull(conv.conversationId)
        assertEquals("peer_alpha", conv.peerId)
        assertEquals("Alpha Node", conv.title)

        val retrieved = repository.getOrCreateConversation(peerId = "peer_alpha", title = "Alpha Node")
        assertEquals(conv.conversationId, retrieved.conversationId)
    }

    @Test
    fun updateLastMessage_updatesConversationDetails() = runTest {
        val conv = repository.getOrCreateConversation(peerId = "peer_beta", title = "Beta Node")
        repository.updateLastMessage(conv.conversationId, "Need assistance", 123456L, MessageStatus.QUEUED)

        val updated = repository.getConversationDirect(conv.conversationId)
        assertNotNull(updated)
        assertEquals("Need assistance", updated?.lastMessage)
        assertEquals(123456L, updated?.lastMessageTimestamp)
        assertEquals(MessageStatus.QUEUED, updated?.lastMessageStatus)
    }
}
