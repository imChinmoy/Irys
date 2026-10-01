package com.irys.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["lastMessageTimestamp"])
    ]
)
data class ConversationEntity(
    @PrimaryKey
    val conversationId: String,
    val peerId: String,
    val title: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Long,
    val unreadCount: Int,
    val lastMessageStatus: String?
)
