package com.irys.app.feature.nearby

import com.irys.app.domain.model.Peer

data class NearbyUiState(
    val peers: List<Peer> = emptyList(),
    val isScanning: Boolean = false,
    val isAdvertising: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isBleSupported: Boolean = true,
    val hasPermissions: Boolean = true,
    val errorMessage: String? = null,
    val connectingPeerId: String? = null
)
