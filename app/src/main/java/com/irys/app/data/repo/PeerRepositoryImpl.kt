package com.irys.app.data.repo

import com.irys.app.core.database.dao.PeerDao
import com.irys.app.data.model.toDomain
import com.irys.app.data.model.toEntity
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import com.irys.app.domain.repo.PeerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeerRepositoryImpl @Inject constructor(
    private val peerDao: PeerDao
) : PeerRepository {

    override fun getPeers(): Flow<List<Peer>> {
        return peerDao.getPeers().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPeer(nodeId: String): Flow<Peer?> {
        return peerDao.getPeerFlow(nodeId).map { it?.toDomain() }
    }

    override suspend fun getPeerDirect(nodeId: String): Peer? {
        return peerDao.getPeerDirect(nodeId)?.toDomain()
    }

    override suspend fun savePeer(peer: Peer) {
        peerDao.insertOrUpdate(peer.toEntity())
    }

    override fun getPeerCount(): Flow<Int> {
        return peerDao.countPeers()
    }

    override suspend fun deletePeer(nodeId: String) {
        peerDao.deletePeer(nodeId)
    }

    override suspend fun updatePeerRssi(nodeId: String, rssi: Int, lastSeenTimestamp: Long) {
        peerDao.updatePeerRssi(nodeId, rssi, lastSeenTimestamp)
    }

    override suspend fun updatePeerConnectionStatus(nodeId: String, status: PeerConnectionStatus) {
        peerDao.updatePeerConnectionStatus(nodeId, status.name)
    }

    override suspend fun removeStalePeers(olderThanTimestamp: Long) {
        peerDao.deleteStalePeers(olderThanTimestamp)
    }
}
