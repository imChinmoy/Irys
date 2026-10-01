package com.irys.app.feature.chat

import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.Message

data class ChatUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)
