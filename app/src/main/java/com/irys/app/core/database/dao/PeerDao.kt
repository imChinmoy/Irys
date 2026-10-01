package com.irys.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.irys.app.core.database.entity.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(peer: PeerEntity)

    @Query("SELECT * FROM peers ORDER BY lastSeenTimestamp DESC")
    fun getPeers(): Flow<List<PeerEntity>>

    @Query("SELECT * FROM peers WHERE nodeId = :nodeId")
    fun getPeerFlow(nodeId: String): Flow<PeerEntity?>

    @Query("SELECT * FROM peers WHERE nodeId = :nodeId")
    suspend fun getPeerDirect(nodeId: String): PeerEntity?

    @Query("SELECT COUNT(*) FROM peers")
    fun countPeers(): Flow<Int>

    @Query("DELETE FROM peers WHERE nodeId = :nodeId")
    suspend fun deletePeer(nodeId: String)

    @Query("UPDATE peers SET rssi = :rssi, lastSeenTimestamp = :lastSeenTimestamp WHERE nodeId = :nodeId")
    suspend fun updatePeerRssi(nodeId: String, rssi: Int, lastSeenTimestamp: Long)

    @Query("UPDATE peers SET connectionStatus = :status WHERE nodeId = :nodeId")
    suspend fun updatePeerConnectionStatus(nodeId: String, status: String)

    @Query("DELETE FROM peers WHERE lastSeenTimestamp < :olderThanTimestamp")
    suspend fun deleteStalePeers(olderThanTimestamp: Long)
}
