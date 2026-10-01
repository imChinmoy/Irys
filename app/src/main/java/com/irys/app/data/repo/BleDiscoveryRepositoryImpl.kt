package com.irys.app.data.repo

import com.irys.app.core.bluetooth.BleManager
import com.irys.app.core.bluetooth.model.AdvertisingState
import com.irys.app.core.bluetooth.model.ScanState
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.repo.BleDiscoveryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleDiscoveryRepositoryImpl @Inject constructor(
    private val bleManager: BleManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : BleDiscoveryRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isAdvertising = MutableStateFlow(false)
    override val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _scanErrorMessage = MutableStateFlow<String?>(null)
    override val scanErrorMessage: StateFlow<String?> = _scanErrorMessage.asStateFlow()

    init {
        scope.launch {
            bleManager.scanState.collectLatest { state ->
                when (state) {
                    is ScanState.Scanning -> {
                        _isScanning.value = true
                        _scanErrorMessage.value = null
                    }
                    is ScanState.Stopped, is ScanState.Idle -> {
                        _isScanning.value = false
                    }
                    is ScanState.Failed -> {
                        _isScanning.value = false
                        _scanErrorMessage.value = state.message
                    }
                }
            }
        }

        scope.launch {
            bleManager.advertisingState.collectLatest { state ->
                when (state) {
                    is AdvertisingState.Advertising -> {
                        _isAdvertising.value = true
                    }
                    is AdvertisingState.Stopped, is AdvertisingState.Idle -> {
                        _isAdvertising.value = false
                    }
                    is AdvertisingState.Failed -> {
                        _isAdvertising.value = false
                    }
                }
            }
        }
    }

    override fun isBleSupported(): Boolean = bleManager.isBleSupported()
    override fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()

    override fun startDiscovery() {
        bleManager.startDiscovery()
    }

    override fun stopDiscovery() {
        bleManager.stopDiscovery()
    }

    override fun startAdvertising(alias: String) {
        bleManager.startAdvertising(alias)
    }

    override fun stopAdvertising() {
        bleManager.stopAdvertising()
    }

    override fun connect(nodeId: String, address: String) {
        bleManager.connect(nodeId, address)
    }

    override fun disconnect(nodeId: String) {
        bleManager.disconnect(nodeId)
    }

    override fun pruneStalePeers() {
        bleManager.pruneStale()
    }
}
