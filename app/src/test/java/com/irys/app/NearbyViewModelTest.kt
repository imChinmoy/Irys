package com.irys.app

import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import com.irys.app.domain.model.PermissionStatus
import com.irys.app.domain.repo.BleDiscoveryRepository
import com.irys.app.domain.repo.PeerRepository
import com.irys.app.domain.repo.PermissionRepository
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import com.irys.app.domain.usecases.peer.ConnectToPeerUseCase
import com.irys.app.domain.usecases.peer.DisconnectPeerUseCase
import com.irys.app.domain.usecases.peer.GetDiscoveryStateUseCase
import com.irys.app.domain.usecases.peer.GetPeersUseCase
import com.irys.app.domain.usecases.peer.StartAdvertisingUseCase
import com.irys.app.domain.usecases.peer.StartDiscoveryUseCase
import com.irys.app.domain.usecases.peer.StopAdvertisingUseCase
import com.irys.app.domain.usecases.peer.StopDiscoveryUseCase
import com.irys.app.feature.nearby.NearbyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePeerRepository : PeerRepository {
        val peersFlow = MutableStateFlow<List<Peer>>(
            listOf(
                Peer(
                    nodeId = "node_112233445566",
                    alias = "Irys Node Alpha",
                    rssi = -65,
                    connectionStatus = PeerConnectionStatus.DISCOVERED
                )
            )
        )

        override fun getPeers(): Flow<List<Peer>> = peersFlow
        override fun getPeer(nodeId: String): Flow<Peer?> = MutableStateFlow(null)
        override suspend fun getPeerDirect(nodeId: String): Peer? = null
        override suspend fun savePeer(peer: Peer) {}
        override fun getPeerCount(): Flow<Int> = MutableStateFlow(1)
        override suspend fun deletePeer(nodeId: String) {}
        override suspend fun updatePeerRssi(nodeId: String, rssi: Int, lastSeenTimestamp: Long) {}
        override suspend fun updatePeerConnectionStatus(nodeId: String, status: PeerConnectionStatus) {
            peersFlow.value = peersFlow.value.map {
                if (it.nodeId == nodeId) it.copy(connectionStatus = status) else it
            }
        }
        override suspend fun removeStalePeers(olderThanTimestamp: Long) {}
    }

    private class FakeBleDiscoveryRepository : BleDiscoveryRepository {
        val scanningFlow = MutableStateFlow(false)
        val advertisingFlow = MutableStateFlow(false)
        val errorFlow = MutableStateFlow<String?>(null)

        var lastConnectedAddress: String? = null
        var lastDisconnectedNodeId: String? = null

        override val isScanning: StateFlow<Boolean> = scanningFlow
        override val isAdvertising: StateFlow<Boolean> = advertisingFlow
        override val scanErrorMessage: StateFlow<String?> = errorFlow

        override fun isBleSupported(): Boolean = true
        override fun isBluetoothEnabled(): Boolean = true

        override fun startDiscovery() {
            scanningFlow.value = true
        }

        override fun stopDiscovery() {
            scanningFlow.value = false
        }

        override fun startAdvertising(alias: String) {
            advertisingFlow.value = true
        }

        override fun stopAdvertising() {
            advertisingFlow.value = false
        }

        override fun connect(nodeId: String, address: String) {
            lastConnectedAddress = address
        }

        override fun disconnect(nodeId: String) {
            lastDisconnectedNodeId = nodeId
        }

        override fun pruneStalePeers() {}
    }

    private class FakePermissionRepository : PermissionRepository {
        override fun getPermissionStatuses(): List<PermissionStatus> = emptyList()
        override fun areRequiredPermissionsGranted(): Boolean = true
        override fun getRequiredPermissionsList(): List<String> = emptyList()
    }

    private class FakeAppSettingsRepository : com.irys.app.domain.repo.AppSettingsRepository {
        override fun isOnboardingCompleted(): Flow<Boolean> = MutableStateFlow(true)
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
        override suspend fun getNodeAlias(): String = "LocalNode"
        override suspend fun setNodeAlias(alias: String) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun nearbyViewModel_observesPeersAndDiscoveryState() = runTest(testDispatcher) {
        val peerRepo = FakePeerRepository()
        val bleRepo = FakeBleDiscoveryRepository()
        val permRepo = FakePermissionRepository()
        val settingsRepo = FakeAppSettingsRepository()

        val getPeersUseCase = GetPeersUseCase(peerRepo)
        val startDiscoveryUseCase = StartDiscoveryUseCase(bleRepo)
        val stopDiscoveryUseCase = StopDiscoveryUseCase(bleRepo)
        val startAdvertisingUseCase = StartAdvertisingUseCase(bleRepo, settingsRepo)
        val stopAdvertisingUseCase = StopAdvertisingUseCase(bleRepo)
        val connectToPeerUseCase = ConnectToPeerUseCase(bleRepo)
        val disconnectPeerUseCase = DisconnectPeerUseCase(bleRepo)
        val getDiscoveryStateUseCase = GetDiscoveryStateUseCase(bleRepo)
        val getPermissionsStatusUseCase = GetPermissionsStatusUseCase(permRepo)

        val viewModel = NearbyViewModel(
            getPeersUseCase,
            startDiscoveryUseCase,
            stopDiscoveryUseCase,
            startAdvertisingUseCase,
            stopAdvertisingUseCase,
            connectToPeerUseCase,
            disconnectPeerUseCase,
            getDiscoveryStateUseCase,
            getPermissionsStatusUseCase,
            testDispatcher
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(1, state.peers.size)
        assertEquals("Irys Node Alpha", state.peers[0].alias)
        assertFalse(state.isScanning)
        assertFalse(state.isAdvertising)
        assertTrue(state.isBluetoothEnabled)
        assertTrue(state.hasPermissions)
    }

    @Test
    fun nearbyViewModel_toggleScan_startsAndStopsScan() = runTest(testDispatcher) {
        val peerRepo = FakePeerRepository()
        val bleRepo = FakeBleDiscoveryRepository()
        val permRepo = FakePermissionRepository()
        val settingsRepo = FakeAppSettingsRepository()

        val viewModel = NearbyViewModel(
            GetPeersUseCase(peerRepo),
            StartDiscoveryUseCase(bleRepo),
            StopDiscoveryUseCase(bleRepo),
            StartAdvertisingUseCase(bleRepo, settingsRepo),
            StopAdvertisingUseCase(bleRepo),
            ConnectToPeerUseCase(bleRepo),
            DisconnectPeerUseCase(bleRepo),
            GetDiscoveryStateUseCase(bleRepo),
            GetPermissionsStatusUseCase(permRepo),
            testDispatcher
        )

        viewModel.toggleScan()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isScanning)

        viewModel.toggleScan()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.first().isScanning)
    }

    @Test
    fun nearbyViewModel_toggleAdvertising_startsAndStopsAdvertising() = runTest(testDispatcher) {
        val peerRepo = FakePeerRepository()
        val bleRepo = FakeBleDiscoveryRepository()
        val permRepo = FakePermissionRepository()
        val settingsRepo = FakeAppSettingsRepository()

        val viewModel = NearbyViewModel(
            GetPeersUseCase(peerRepo),
            StartDiscoveryUseCase(bleRepo),
            StopDiscoveryUseCase(bleRepo),
            StartAdvertisingUseCase(bleRepo, settingsRepo),
            StopAdvertisingUseCase(bleRepo),
            ConnectToPeerUseCase(bleRepo),
            DisconnectPeerUseCase(bleRepo),
            GetDiscoveryStateUseCase(bleRepo),
            GetPermissionsStatusUseCase(permRepo),
            testDispatcher
        )

        viewModel.toggleAdvertising()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isAdvertising)

        viewModel.toggleAdvertising()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.first().isAdvertising)
    }

    @Test
    fun nearbyViewModel_connectAndDisconnectPeer() = runTest(testDispatcher) {
        val peerRepo = FakePeerRepository()
        val bleRepo = FakeBleDiscoveryRepository()
        val permRepo = FakePermissionRepository()
        val settingsRepo = FakeAppSettingsRepository()

        val viewModel = NearbyViewModel(
            GetPeersUseCase(peerRepo),
            StartDiscoveryUseCase(bleRepo),
            StopDiscoveryUseCase(bleRepo),
            StartAdvertisingUseCase(bleRepo, settingsRepo),
            StopAdvertisingUseCase(bleRepo),
            ConnectToPeerUseCase(bleRepo),
            DisconnectPeerUseCase(bleRepo),
            GetDiscoveryStateUseCase(bleRepo),
            GetPermissionsStatusUseCase(permRepo),
            testDispatcher
        )

        val peer = Peer(
            nodeId = "node_112233445566",
            alias = "Test Node",
            rssi = -60
        )

        viewModel.connectToPeer(peer)
        assertEquals("11:22:33:44:55:66", bleRepo.lastConnectedAddress)

        viewModel.disconnectPeer(peer)
        assertEquals("node_112233445566", bleRepo.lastDisconnectedNodeId)
    }
}
