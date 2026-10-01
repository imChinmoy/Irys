package com.irys.app

import com.irys.app.core.ui.components.StatusIndicatorType
import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PermissionStatus
import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.repo.ConversationRepository
import com.irys.app.domain.repo.MessageRepository
import com.irys.app.domain.repo.PeerRepository
import com.irys.app.domain.repo.PermissionRepository
import com.irys.app.domain.usecases.app.GetNodeAliasUseCase
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import com.irys.app.domain.usecases.conversation.GetConversationCountUseCase
import com.irys.app.domain.usecases.message.GetPendingMessageCountUseCase
import com.irys.app.domain.usecases.peer.GetPeerCountUseCase
import com.irys.app.feature.home.HomeViewModel
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAppSettingsRepository : AppSettingsRepository {
        var alias = "AlphaNode"

        override fun isOnboardingCompleted(): Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun getNodeAlias(): String = alias
        override suspend fun setNodeAlias(alias: String) {
            this.alias = alias
        }
    }

    private class FakePermissionRepository(var requiredGranted: Boolean) : PermissionRepository {
        override fun getPermissionStatuses(): List<PermissionStatus> = emptyList()
        override fun areRequiredPermissionsGranted(): Boolean = requiredGranted
        override fun getRequiredPermissionsList(): List<String> = emptyList()
    }

    private class FakeMessageRepository : MessageRepository {
        override fun getMessagesForConversation(conversationId: String): Flow<List<Message>> = MutableStateFlow(emptyList())
        override fun getAllMessages(): Flow<List<Message>> = MutableStateFlow(emptyList())
        override fun getPendingMessageCount(): Flow<Int> = MutableStateFlow(3)
        override suspend fun getMessageById(messageId: String): Message? = null
        override suspend fun saveMessage(message: Message) {}
        override suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {}
        override suspend fun deleteMessage(messageId: String) {}
        override suspend fun createLocalMessage(conversationId: String, destinationId: String, payload: String, priority: MessagePriority): Message {
            throw UnsupportedOperationException()
        }
    }

    private class FakeConversationRepository : ConversationRepository {
        override fun getConversations(): Flow<List<Conversation>> = MutableStateFlow(emptyList())
        override fun getConversation(conversationId: String): Flow<Conversation?> = MutableStateFlow(null)
        override suspend fun getConversationDirect(conversationId: String): Conversation? = null
        override suspend fun getOrCreateConversation(peerId: String, title: String): Conversation = throw UnsupportedOperationException()
        override suspend fun saveConversation(conversation: Conversation) {}
        override suspend fun updateLastMessage(conversationId: String, lastMessage: String, timestamp: Long, status: MessageStatus) {}
        override suspend fun markAsRead(conversationId: String) {}
        override suspend fun deleteConversation(conversationId: String) {}
        override fun getConversationCount(): Flow<Int> = MutableStateFlow(5)
    }

    private class FakePeerRepository : PeerRepository {
        override fun getPeers(): Flow<List<Peer>> = MutableStateFlow(emptyList())
        override fun getPeer(nodeId: String): Flow<Peer?> = MutableStateFlow(null)
        override suspend fun getPeerDirect(nodeId: String): Peer? = null
        override suspend fun savePeer(peer: Peer) {}
        override fun getPeerCount(): Flow<Int> = MutableStateFlow(2)
        override suspend fun deletePeer(nodeId: String) {}
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
    fun homeViewModel_onlineState_whenPermissionsGranted() = runTest(testDispatcher) {
        val settingsRepo = FakeAppSettingsRepository()
        val permRepo = FakePermissionRepository(requiredGranted = true)
        val msgRepo = FakeMessageRepository()
        val convRepo = FakeConversationRepository()
        val peerRepo = FakePeerRepository()

        val getNodeAliasUseCase = GetNodeAliasUseCase(settingsRepo)
        val getPermissionsStatusUseCase = GetPermissionsStatusUseCase(permRepo)
        val getPendingMessageCountUseCase = GetPendingMessageCountUseCase(msgRepo)
        val getConversationCountUseCase = GetConversationCountUseCase(convRepo)
        val getPeerCountUseCase = GetPeerCountUseCase(peerRepo)

        val viewModel = HomeViewModel(
            getNodeAliasUseCase,
            getPermissionsStatusUseCase,
            getPendingMessageCountUseCase,
            getConversationCountUseCase,
            getPeerCountUseCase,
            testDispatcher
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("AlphaNode", state.nodeAlias)
        assertEquals(StatusIndicatorType.ONLINE, state.meshStatus)
        assertEquals("Mesh Active", state.meshStatusLabel)
        assertTrue(state.bluetoothPermissionsGranted)
        assertEquals(3, state.pendingMessageCount)
        assertEquals(5, state.recentConversationsCount)
        assertEquals(2, state.nearbyPeerCount)
    }

    @Test
    fun homeViewModel_degradedState_whenPermissionsMissing() = runTest(testDispatcher) {
        val settingsRepo = FakeAppSettingsRepository()
        val permRepo = FakePermissionRepository(requiredGranted = false)
        val msgRepo = FakeMessageRepository()
        val convRepo = FakeConversationRepository()
        val peerRepo = FakePeerRepository()

        val getNodeAliasUseCase = GetNodeAliasUseCase(settingsRepo)
        val getPermissionsStatusUseCase = GetPermissionsStatusUseCase(permRepo)
        val getPendingMessageCountUseCase = GetPendingMessageCountUseCase(msgRepo)
        val getConversationCountUseCase = GetConversationCountUseCase(convRepo)
        val getPeerCountUseCase = GetPeerCountUseCase(peerRepo)

        val viewModel = HomeViewModel(
            getNodeAliasUseCase,
            getPermissionsStatusUseCase,
            getPendingMessageCountUseCase,
            getConversationCountUseCase,
            getPeerCountUseCase,
            testDispatcher
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals("AlphaNode", state.nodeAlias)
        assertEquals(StatusIndicatorType.DEGRADED, state.meshStatus)
        assertEquals("Degraded (No Permissions)", state.meshStatusLabel)
        assertFalse(state.bluetoothPermissionsGranted)
    }
}
