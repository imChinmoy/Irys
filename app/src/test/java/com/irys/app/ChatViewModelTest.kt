package com.irys.app

import androidx.lifecycle.SavedStateHandle
import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.model.MessageType
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import com.irys.app.domain.usecases.conversation.GetConversationByIdUseCase
import com.irys.app.domain.usecases.conversation.MarkConversationAsReadUseCase
import com.irys.app.domain.usecases.message.GetMessagesForConversationUseCase
import com.irys.app.domain.usecases.message.SendMessageUseCase
import com.irys.app.domain.usecases.message.UpdateMessageStatusUseCase
import com.irys.app.feature.chat.ChatViewModel
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeMessageRepository : MessageRepository {
        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        val messageMap = mutableMapOf<String, Message>()

        override fun getMessagesForConversation(conversationId: String): Flow<List<Message>> = messagesFlow
        override fun getAllMessages(): Flow<List<Message>> = messagesFlow
        override fun getPendingMessageCount(): Flow<Int> = MutableStateFlow(0)
        override suspend fun getMessageById(messageId: String): Message? = messageMap[messageId]
        override suspend fun saveMessage(message: Message) {}
        override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
            val existing = messageMap[messageId]
            if (existing != null) {
                val updated = existing.copy(status = status)
                messageMap[messageId] = updated
                messagesFlow.value = messageMap.values.toList()
            }
        }
        override suspend fun deleteMessage(messageId: String) {}

        override suspend fun createLocalMessage(
            conversationId: String,
            destinationId: String,
            payload: String,
            priority: MessagePriority
        ): Message {
            val msg = Message(
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
            messageMap[msg.messageId] = msg
            messagesFlow.value = messageMap.values.toList()
            return msg
        }
    }

    private class FakeConversationRepository : ConversationRepository {
        val conversation = Conversation(
            conversationId = "conv_test",
            peerId = "peer_test",
            title = "Peer Test"
        )
        val convFlow = MutableStateFlow<Conversation?>(conversation)

        override fun getConversations(): Flow<List<Conversation>> = MutableStateFlow(listOf(conversation))
        override fun getConversation(conversationId: String): Flow<Conversation?> = convFlow
        override suspend fun getConversationDirect(conversationId: String): Conversation? = conversation
        override suspend fun getOrCreateConversation(peerId: String, title: String): Conversation = conversation
        override suspend fun saveConversation(conversation: Conversation) {}
        override suspend fun updateLastMessage(conversationId: String, lastMessage: String, timestamp: Long, status: MessageStatus) {
            convFlow.value = conversation.copy(lastMessage = lastMessage, lastMessageTimestamp = timestamp, lastMessageStatus = status)
        }
        override suspend fun markAsRead(conversationId: String) {}
        override suspend fun deleteConversation(conversationId: String) {}
        override fun getConversationCount(): Flow<Int> = MutableStateFlow(1)
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
    fun chatViewModel_observesConversationAndSendsMessage() = runTest(testDispatcher) {
        val messageRepo = FakeMessageRepository()
        val convRepo = FakeConversationRepository()

        val getConversationByIdUseCase = GetConversationByIdUseCase(convRepo)
        val getMessagesForConversationUseCase = GetMessagesForConversationUseCase(messageRepo)
        val sendMessageUseCase = SendMessageUseCase(messageRepo, convRepo)
        val updateMessageStatusUseCase = UpdateMessageStatusUseCase(messageRepo, convRepo)
        val markConversationAsReadUseCase = MarkConversationAsReadUseCase(convRepo)

        val savedStateHandle = SavedStateHandle(mapOf("conversationId" to "conv_test"))
        val viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            getConversationByIdUseCase = getConversationByIdUseCase,
            getMessagesForConversationUseCase = getMessagesForConversationUseCase,
            sendMessageUseCase = sendMessageUseCase,
            updateMessageStatusUseCase = updateMessageStatusUseCase,
            markConversationAsReadUseCase = markConversationAsReadUseCase,
            ioDispatcher = testDispatcher
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Peer Test", viewModel.uiState.first().conversation?.title)
        assertTrue(viewModel.uiState.first().messages.isEmpty())

        viewModel.onInputTextChanged("Hello test")
        assertEquals("Hello test", viewModel.uiState.first().inputText)

        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertEquals(1, state.messages.size)
        assertEquals("Hello test", state.messages[0].payload)
        assertEquals(MessageStatus.QUEUED, state.messages[0].status)
    }

    @Test
    fun chatViewModel_updateMessageStatus_updatesFlow() = runTest(testDispatcher) {
        val messageRepo = FakeMessageRepository()
        val convRepo = FakeConversationRepository()

        val getConversationByIdUseCase = GetConversationByIdUseCase(convRepo)
        val getMessagesForConversationUseCase = GetMessagesForConversationUseCase(messageRepo)
        val sendMessageUseCase = SendMessageUseCase(messageRepo, convRepo)
        val updateMessageStatusUseCase = UpdateMessageStatusUseCase(messageRepo, convRepo)
        val markConversationAsReadUseCase = MarkConversationAsReadUseCase(convRepo)

        val savedStateHandle = SavedStateHandle(mapOf("conversationId" to "conv_test"))
        val viewModel = ChatViewModel(
            savedStateHandle = savedStateHandle,
            getConversationByIdUseCase = getConversationByIdUseCase,
            getMessagesForConversationUseCase = getMessagesForConversationUseCase,
            sendMessageUseCase = sendMessageUseCase,
            updateMessageStatusUseCase = updateMessageStatusUseCase,
            markConversationAsReadUseCase = markConversationAsReadUseCase,
            ioDispatcher = testDispatcher
        )

        viewModel.onInputTextChanged("Status update test")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val msgId = viewModel.uiState.first().messages[0].messageId
        assertEquals(MessageStatus.QUEUED, viewModel.uiState.first().messages[0].status)

        viewModel.updateMessageStatus(msgId, MessageStatus.DELIVERED)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MessageStatus.DELIVERED, viewModel.uiState.first().messages[0].status)

        viewModel.retryMessage(msgId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(MessageStatus.QUEUED, viewModel.uiState.first().messages[0].status)
    }
}
