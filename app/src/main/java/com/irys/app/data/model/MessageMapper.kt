package com.irys.app.data.model

import com.irys.app.core.database.entity.MessageEntity
import com.irys.app.domain.model.Message
import com.irys.app.domain.model.MessagePriority
import com.irys.app.domain.model.MessageStatus
import com.irys.app.domain.model.MessageType

fun MessageEntity.toDomain(): Message {
    return Message(
        messageId = messageId,
        sourceId = sourceId,
        destinationId = destinationId,
        conversationId = conversationId,
        type = try { MessageType.valueOf(type) } catch (e: IllegalArgumentException) { MessageType.TEXT },
        payload = payload,
        priority = try { MessagePriority.valueOf(priority) } catch (e: IllegalArgumentException) { MessagePriority.NORMAL },
        createdAt = createdAt,
        expiresAt = expiresAt,
        hopCount = hopCount,
        maxHops = maxHops,
        status = try { MessageStatus.valueOf(status) } catch (e: IllegalArgumentException) { MessageStatus.CREATED },
        isOutgoing = isOutgoing
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        messageId = messageId,
        sourceId = sourceId,
        destinationId = destinationId,
        conversationId = conversationId,
        type = type.name,
        payload = payload,
        priority = priority.name,
        createdAt = createdAt,
        expiresAt = expiresAt,
        hopCount = hopCount,
        maxHops = maxHops,
        status = status.name,
        isOutgoing = isOutgoing
    )
}
