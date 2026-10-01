package com.irys.app.feature.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.usecases.conversation.GetConversationsUseCase
import com.irys.app.domain.usecases.conversation.GetOrCreateConversationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getOrCreateConversationUseCase: GetOrCreateConversationUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsUiState(isLoading = true))
    val uiState: StateFlow<ChatsUiState> = _uiState.asStateFlow()

    init {
        observeConversations()
    }

    private fun observeConversations() {
        viewModelScope.launch(ioDispatcher) {
            getConversationsUseCase().collectLatest { conversations ->
                _uiState.update {
                    it.copy(
                        conversations = conversations,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNewChatClick() {
        _uiState.update { it.copy(showNewChatDialog = true) }
    }

    fun onDismissNewChatDialog() {
        _uiState.update { it.copy(showNewChatDialog = false) }
    }

    fun createNewConversation(peerName: String, onCreated: (String) -> Unit) {
        val trimmed = peerName.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(ioDispatcher) {
            val conversation = getOrCreateConversationUseCase(
                peerId = trimmed.lowercase().replace(" ", "_"),
                title = trimmed
            )
            _uiState.update { it.copy(showNewChatDialog = false) }
            onCreated(conversation.conversationId)
        }
    }
}
