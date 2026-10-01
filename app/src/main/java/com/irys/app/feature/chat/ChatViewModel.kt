package com.irys.app.feature.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.usecases.conversation.GetConversationByIdUseCase
import com.irys.app.domain.usecases.conversation.MarkConversationAsReadUseCase
import com.irys.app.domain.usecases.message.GetMessagesForConversationUseCase
import com.irys.app.domain.usecases.message.SendMessageUseCase
import com.irys.app.domain.usecases.message.UpdateMessageStatusUseCase
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
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getConversationByIdUseCase: GetConversationByIdUseCase,
    private val getMessagesForConversationUseCase: GetMessagesForConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val updateMessageStatusUseCase: UpdateMessageStatusUseCase,
    private val markConversationAsReadUseCase: MarkConversationAsReadUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        observeConversation()
        observeMessages()
        markAsRead()
    }

    private fun observeConversation() {
        viewModelScope.launch(ioDispatcher) {
            getConversationByIdUseCase(conversationId).collectLatest { conversation ->
                _uiState.update { it.copy(conversation = conversation) }
            }
        }
    }

    private fun observeMessages() {
        viewModelScope.launch(ioDispatcher) {
            getMessagesForConversationUseCase(conversationId).collectLatest { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun markAsRead() {
        viewModelScope.launch(ioDispatcher) {
            markConversationAsReadUseCase(conversationId)
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage(priority: MessagePriority = MessagePriority.NORMAL) {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        _uiState.update { it.copy(inputText = "", isSending = true, error = null) }

        viewModelScope.launch(ioDispatcher) {
            try {
                val destinationId = _uiState.value.conversation?.peerId ?: "peer"
                sendMessageUseCase(
                    conversationId = conversationId,
                    destinationId = destinationId,
                    payload = text,
                    priority = priority
                )
                _uiState.update { it.copy(isSending = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSending = false, error = e.message ?: "Failed to send message") }
            }
        }
    }

    fun updateMessageStatus(messageId: String, status: MessageStatus) {
        viewModelScope.launch(ioDispatcher) {
            updateMessageStatusUseCase(messageId, status)
        }
    }

    fun retryMessage(messageId: String) {
        updateMessageStatus(messageId, MessageStatus.QUEUED)
    }
}
