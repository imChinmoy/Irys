package com.irys.app.core.bluetooth

import com.irys.app.core.bluetooth.advertiser.BleAdvertiser
import com.irys.app.core.bluetooth.connection.BleConnectionManager
import com.irys.app.core.bluetooth.model.AdvertisingState
import com.irys.app.core.bluetooth.model.BleConnectionState
import com.irys.app.core.bluetooth.model.DiscoveredDevice
import com.irys.app.core.bluetooth.model.ScanState
import com.irys.app.core.bluetooth.scanner.BleScanner
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import com.irys.app.domain.repo.PeerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    val bleScanner: BleScanner,
    val bleAdvertiser: BleAdvertiser,
    val bleConnectionManager: BleConnectionManager,
    private val peerRepository: PeerRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    val scanState: StateFlow<ScanState> = bleScanner.scanState
    val advertisingState: StateFlow<AdvertisingState> = bleAdvertiser.advertisingState
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = bleScanner.discoveredDevices
    val connectionStates: StateFlow<Map<String, BleConnectionState>> = bleConnectionManager.connectionStates

    fun isBleSupported(): Boolean = bleScanner.isSupported()
    fun isBluetoothEnabled(): Boolean = bleScanner.isBluetoothEnabled()
    fun hasPermissions(): Boolean = bleScanner.hasPermissions()

    fun startDiscovery() {
        bleScanner.startScan { device ->
            scope.launch {
                peerRepository.savePeer(
                    Peer(
                        nodeId = device.nodeId,
                        alias = device.alias,
                        lastSeenTimestamp = device.lastSeenTimestamp,
                        isDirect = true,
                        hopDistance = 1,
                        rssi = device.rssi,
                        connectionStatus = PeerConnectionStatus.DISCOVERED
                    )
                )
            }
        }
    }

    fun stopDiscovery() {
        bleScanner.stopScan()
    }

    fun startAdvertising(nodeAlias: String) {
        bleAdvertiser.startAdvertising(nodeAlias)
    }

    fun stopAdvertising() {
        bleAdvertiser.stopAdvertising()
    }

    fun connect(nodeId: String, address: String) {
        bleConnectionManager.connect(nodeId, address)
        scope.launch {
            peerRepository.updatePeerConnectionStatus(nodeId, PeerConnectionStatus.CONNECTING)
        }
    }

    fun disconnect(nodeId: String) {
        bleConnectionManager.disconnect(nodeId)
        scope.launch {
            peerRepository.updatePeerConnectionStatus(nodeId, PeerConnectionStatus.DISCONNECTED)
        }
    }

    fun pruneStale(timeoutMillis: Long = 30000L) {
        bleScanner.pruneStaleDevices(timeoutMillis)
        scope.launch {
            peerRepository.removeStalePeers(System.currentTimeMillis() - timeoutMillis)
        }
    }

    fun release() {
        stopDiscovery()
        stopAdvertising()
        bleConnectionManager.disconnectAll()
    }
}
