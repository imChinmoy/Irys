package com.irys.app.feature.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import com.irys.app.domain.usecases.peer.ConnectToPeerUseCase
import com.irys.app.domain.usecases.peer.DisconnectPeerUseCase
import com.irys.app.domain.usecases.peer.GetDiscoveryStateUseCase
import com.irys.app.domain.usecases.peer.GetPeersUseCase
import com.irys.app.domain.usecases.peer.StartAdvertisingUseCase
import com.irys.app.domain.usecases.peer.StartDiscoveryUseCase
import com.irys.app.domain.usecases.peer.StopAdvertisingUseCase
import com.irys.app.domain.usecases.peer.StopDiscoveryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val getPeersUseCase: GetPeersUseCase,
    private val startDiscoveryUseCase: StartDiscoveryUseCase,
    private val stopDiscoveryUseCase: StopDiscoveryUseCase,
    private val startAdvertisingUseCase: StartAdvertisingUseCase,
    private val stopAdvertisingUseCase: StopAdvertisingUseCase,
    private val connectToPeerUseCase: ConnectToPeerUseCase,
    private val disconnectPeerUseCase: DisconnectPeerUseCase,
    private val getDiscoveryStateUseCase: GetDiscoveryStateUseCase,
    private val getPermissionsStatusUseCase: GetPermissionsStatusUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    init {
        refreshHardwareState()
        observePeers()
        observeDiscoveryState()
    }

    fun refreshHardwareState() {
        val hasPerms = getPermissionsStatusUseCase.areRequiredGranted()
        val isBtEnabled = getDiscoveryStateUseCase.isBluetoothEnabled()
        val isBleSupported = getDiscoveryStateUseCase.isBleSupported()

        _uiState.update {
            it.copy(
                hasPermissions = hasPerms,
                isBluetoothEnabled = isBtEnabled,
                isBleSupported = isBleSupported
            )
        }
    }

    private fun observePeers() {
        viewModelScope.launch(ioDispatcher) {
            getPeersUseCase().collectLatest { peers ->
                _uiState.update { it.copy(peers = peers) }
            }
        }
    }

    private fun observeDiscoveryState() {
        viewModelScope.launch(ioDispatcher) {
            launch {
                getDiscoveryStateUseCase.isScanning.collectLatest { scanning ->
                    _uiState.update { it.copy(isScanning = scanning) }
                }
            }
            launch {
                getDiscoveryStateUseCase.isAdvertising.collectLatest { advertising ->
                    _uiState.update { it.copy(isAdvertising = advertising) }
                }
            }
            launch {
                getDiscoveryStateUseCase.scanErrorMessage.collectLatest { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            }
        }
    }

    fun toggleScan() {
        refreshHardwareState()
        if (_uiState.value.isScanning) {
            stopDiscoveryUseCase()
        } else {
            startDiscoveryUseCase()
        }
    }

    fun toggleAdvertising() {
        refreshHardwareState()
        viewModelScope.launch(ioDispatcher) {
            if (_uiState.value.isAdvertising) {
                stopAdvertisingUseCase()
            } else {
                startAdvertisingUseCase()
            }
        }
    }

    fun connectToPeer(peer: Peer) {
        val address = peer.nodeId.removePrefix("node_")
        val formattedAddress = if (address.length == 12) {
            address.chunked(2).joinToString(":")
        } else {
            peer.nodeId
        }
        connectToPeerUseCase(peer.nodeId, formattedAddress)
    }

    fun disconnectPeer(peer: Peer) {
        disconnectPeerUseCase(peer.nodeId)
    }
}
