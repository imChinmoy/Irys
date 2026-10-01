package com.irys.app.data.model

import com.irys.app.core.database.entity.ConversationEntity
import com.irys.app.domain.model.Conversation
import com.irys.app.domain.model.MessageStatus

fun ConversationEntity.toDomain(): Conversation {
    return Conversation(
        conversationId = conversationId,
        peerId = peerId,
        title = title,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        lastMessageStatus = lastMessageStatus?.let {
            try { MessageStatus.valueOf(it) } catch (e: IllegalArgumentException) { null }
        }
    )
}

fun Conversation.toEntity(): ConversationEntity {
    return ConversationEntity(
        conversationId = conversationId,
        peerId = peerId,
        title = title,
        lastMessage = lastMessage,
        lastMessageTimestamp = lastMessageTimestamp,
        unreadCount = unreadCount,
        lastMessageStatus = lastMessageStatus?.name
    )
}
