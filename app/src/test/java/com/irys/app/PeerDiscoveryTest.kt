package com.irys.app

import com.irys.app.core.database.dao.PeerDao
import com.irys.app.core.database.entity.PeerEntity
import com.irys.app.data.repo.PeerRepositoryImpl
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PeerDiscoveryTest {

    private class FakePeerDao : PeerDao {
        val peers = mutableListOf<PeerEntity>()
        val peersFlow = MutableStateFlow<List<PeerEntity>>(emptyList())

        private fun notifyChange() {
            peersFlow.value = peers.toList()
        }

        override suspend fun insertOrUpdate(peer: PeerEntity) {
            peers.removeAll { it.nodeId == peer.nodeId }
            peers.add(peer)
            notifyChange()
        }

        override fun getPeers(): Flow<List<PeerEntity>> = peersFlow

        override fun getPeerFlow(nodeId: String): Flow<PeerEntity?> {
            return peersFlow.map { list -> list.find { it.nodeId == nodeId } }
        }

        override suspend fun getPeerDirect(nodeId: String): PeerEntity? {
            return peers.find { it.nodeId == nodeId }
        }

        override fun countPeers(): Flow<Int> = peersFlow.map { it.size }

        override suspend fun deletePeer(nodeId: String) {
            peers.removeAll { it.nodeId == nodeId }
            notifyChange()
        }

        override suspend fun updatePeerRssi(nodeId: String, rssi: Int, lastSeenTimestamp: Long) {
            val index = peers.indexOfFirst { it.nodeId == nodeId }
            if (index != -1) {
                peers[index] = peers[index].copy(rssi = rssi, lastSeenTimestamp = lastSeenTimestamp)
                notifyChange()
            }
        }

        override suspend fun updatePeerConnectionStatus(nodeId: String, status: String) {
            val index = peers.indexOfFirst { it.nodeId == nodeId }
            if (index != -1) {
                peers[index] = peers[index].copy(connectionStatus = status)
                notifyChange()
            }
        }

        override suspend fun deleteStalePeers(olderThanTimestamp: Long) {
            peers.removeAll { it.lastSeenTimestamp < olderThanTimestamp }
            notifyChange()
        }
    }

    private lateinit var dao: FakePeerDao
    private lateinit var repository: PeerRepositoryImpl

    @Before
    fun setUp() {
        dao = FakePeerDao()
        repository = PeerRepositoryImpl(dao)
    }

    @Test
    fun peerDiscovery_deduplicatesByNodeId() = runTest {
        val peer1 = Peer(nodeId = "node_a", alias = "Alpha", rssi = -75, lastSeenTimestamp = 1000L)
        val peer1Updated = Peer(nodeId = "node_a", alias = "Alpha", rssi = -62, lastSeenTimestamp = 2000L)

        repository.savePeer(peer1)
        assertEquals(1, repository.getPeers().first().size)
        assertEquals(-75, repository.getPeers().first()[0].rssi)

        // Multiple discovery packets for the same device deduplicate into a single entry
        repository.savePeer(peer1Updated)
        val peers = repository.getPeers().first()
        assertEquals(1, peers.size)
        assertEquals(-62, peers[0].rssi)
        assertEquals(2000L, peers[0].lastSeenTimestamp)
    }

    @Test
    fun peerDiscovery_updatePeerConnectionStatus_transitionsCorrectly() = runTest {
        val peer = Peer(nodeId = "node_b", alias = "Beta", rssi = -70)
        repository.savePeer(peer)

        assertEquals(PeerConnectionStatus.DISCOVERED, repository.getPeers().first()[0].connectionStatus)

        repository.updatePeerConnectionStatus("node_b", PeerConnectionStatus.CONNECTING)
        assertEquals(PeerConnectionStatus.CONNECTING, repository.getPeers().first()[0].connectionStatus)

        repository.updatePeerConnectionStatus("node_b", PeerConnectionStatus.READY)
        assertEquals(PeerConnectionStatus.READY, repository.getPeers().first()[0].connectionStatus)
    }

    @Test
    fun peerDiscovery_removeStalePeers_cleansInactiveNodes() = runTest {
        val freshPeer = Peer(nodeId = "node_fresh", alias = "Fresh", lastSeenTimestamp = 5000L)
        val stalePeer = Peer(nodeId = "node_stale", alias = "Stale", lastSeenTimestamp = 1000L)

        repository.savePeer(freshPeer)
        repository.savePeer(stalePeer)
        assertEquals(2, repository.getPeers().first().size)

        repository.removeStalePeers(olderThanTimestamp = 3000L)
        val remaining = repository.getPeers().first()
        assertEquals(1, remaining.size)
        assertEquals("node_fresh", remaining[0].nodeId)
    }
}
