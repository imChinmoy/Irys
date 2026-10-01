package com.irys.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.irys.app.core.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY lastMessageTimestamp DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    fun getConversationFlow(conversationId: String): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE conversationId = :conversationId")
    suspend fun getConversationDirect(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE peerId = :peerId LIMIT 1")
    suspend fun getConversationByPeerId(peerId: String): ConversationEntity?

    @Query("UPDATE conversations SET lastMessage = :lastMessage, lastMessageTimestamp = :timestamp, lastMessageStatus = :status WHERE conversationId = :conversationId")
    suspend fun updateLastMessage(
        conversationId: String,
        lastMessage: String,
        timestamp: Long,
        status: String
    )

    @Query("UPDATE conversations SET unreadCount = 0 WHERE conversationId = :conversationId")
    suspend fun markAsRead(conversationId: String)

    @Query("DELETE FROM conversations WHERE conversationId = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("SELECT COUNT(*) FROM conversations")
    fun countConversations(): Flow<Int>
}
