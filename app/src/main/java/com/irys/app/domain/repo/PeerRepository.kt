package com.irys.app.domain.repo

import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import kotlinx.coroutines.flow.Flow

interface PeerRepository {
    fun getPeers(): Flow<List<Peer>>
    fun getPeer(nodeId: String): Flow<Peer?>
    suspend fun getPeerDirect(nodeId: String): Peer?
    suspend fun savePeer(peer: Peer)
    fun getPeerCount(): Flow<Int>
    suspend fun deletePeer(nodeId: String)
    suspend fun updatePeerRssi(nodeId: String, rssi: Int, lastSeenTimestamp: Long)
    suspend fun updatePeerConnectionStatus(nodeId: String, status: PeerConnectionStatus)
    suspend fun removeStalePeers(olderThanTimestamp: Long)
}
