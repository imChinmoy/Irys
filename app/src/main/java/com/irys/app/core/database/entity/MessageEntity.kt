package com.irys.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["createdAt"]),
        Index(value = ["status"])
    ]
)
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val sourceId: String,
    val destinationId: String,
    val conversationId: String,
    val type: String,
    val payload: String,
    val priority: String,
    val createdAt: Long,
    val expiresAt: Long,
    val hopCount: Int,
    val maxHops: Int,
    val status: String,
    val isOutgoing: Boolean
)
