package com.irys.app

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.usecases.conversation.GetConversationsUseCase
import com.irys.app.domain.usecases.conversation.GetOrCreateConversationUseCase
import com.irys.app.feature.chats.ChatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeConversationRepository : ConversationRepository {
        val conversations = mutableListOf(
            Conversation(
                conversationId = "conv_1",
                peerId = "peer_1",
                title = "Node 1",
                lastMessage = "Signal check",
                lastMessageTimestamp = 1000L,
                lastMessageStatus = MessageStatus.DELIVERED
            )
        )
        val convsFlow = MutableStateFlow<List<Conversation>>(conversations)

        override fun getConversations(): Flow<List<Conversation>> = convsFlow
        override fun getConversation(conversationId: String): Flow<Conversation?> = MutableStateFlow(null)
        override suspend fun getConversationDirect(conversationId: String): Conversation? = null
        override suspend fun getOrCreateConversation(peerId: String, title: String): Conversation {
            val conv = Conversation(
                conversationId = "conv_new",
                peerId = peerId,
                title = title
            )
            conversations.add(conv)
            convsFlow.value = conversations.toList()
            return conv
        }

        override suspend fun saveConversation(conversation: Conversation) {}
        override suspend fun updateLastMessage(conversationId: String, lastMessage: String, timestamp: Long, status: MessageStatus) {}
        override suspend fun markAsRead(conversationId: String) {}
        override suspend fun deleteConversation(conversationId: String) {}
        override fun getConversationCount(): Flow<Int> = MutableStateFlow(conversations.size)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun chatsViewModel_observesConversationsList() = runTest(testDispatcher) {
        val repo = FakeConversationRepository()
        val getConversationsUseCase = GetConversationsUseCase(repo)
        val getOrCreateConversationUseCase = GetOrCreateConversationUseCase(repo)

        val viewModel = ChatsViewModel(getConversationsUseCase, getOrCreateConversationUseCase, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(1, state.conversations.size)
        assertEquals("Node 1", state.conversations[0].title)
    }

    @Test
    fun chatsViewModel_dialogAndNewConversationCreation() = runTest(testDispatcher) {
        val repo = FakeConversationRepository()
        val getConversationsUseCase = GetConversationsUseCase(repo)
        val getOrCreateConversationUseCase = GetOrCreateConversationUseCase(repo)

        val viewModel = ChatsViewModel(getConversationsUseCase, getOrCreateConversationUseCase, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.first().showNewChatDialog)
        viewModel.onNewChatClick()
        assertTrue(viewModel.uiState.first().showNewChatDialog)

        var createdConvId: String? = null
        viewModel.createNewConversation("New Peer") { id ->
            createdConvId = id
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("conv_new", createdConvId)
        assertFalse(viewModel.uiState.first().showNewChatDialog)
        assertEquals(2, viewModel.uiState.first().conversations.size)
    }
}
