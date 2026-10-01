package com.irys.app.feature.chats

import com.irys.app.domain.model.Conversation

data class ChatsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val showNewChatDialog: Boolean = false
)
