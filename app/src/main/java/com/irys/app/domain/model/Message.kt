package com.irys.app.domain.model

enum class MessageType {
    TEXT,
    EMERGENCY,
    ACK,
    SYSTEM
}

enum class MessagePriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

enum class MessageStatus {
    CREATED,
    QUEUED,
    STORED,
    TRANSFERRING,
    RECEIVED,
    FORWARDED,
    DELIVERED,
    FAILED,
    EXPIRED
}

data class Message(
    val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val conversationId: String,
    val type: MessageType = MessageType.TEXT,
    val payload: String,
    val priority: MessagePriority = MessagePriority.NORMAL,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val hopCount: Int = 0,
    val maxHops: Int = 7,
    val status: MessageStatus = MessageStatus.CREATED,
    val isOutgoing: Boolean = true
)
