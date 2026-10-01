package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.BleDiscoveryRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetDiscoveryStateUseCase @Inject constructor(
    private val bleDiscoveryRepository: BleDiscoveryRepository
) {
    val isScanning: StateFlow<Boolean> = bleDiscoveryRepository.isScanning
    val isAdvertising: StateFlow<Boolean> = bleDiscoveryRepository.isAdvertising
    val scanErrorMessage: StateFlow<String?> = bleDiscoveryRepository.scanErrorMessage

    fun isBleSupported(): Boolean = bleDiscoveryRepository.isBleSupported()
    fun isBluetoothEnabled(): Boolean = bleDiscoveryRepository.isBluetoothEnabled()
}
