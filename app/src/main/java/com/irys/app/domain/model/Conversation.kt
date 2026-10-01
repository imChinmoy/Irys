package com.irys.app.domain.model

data class Conversation(
    val conversationId: String,
    val peerId: String,
    val title: String,
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val lastMessageStatus: MessageStatus? = null
)
