package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.BleDiscoveryRepository
import javax.inject.Inject

class ConnectToPeerUseCase @Inject constructor(
    private val bleDiscoveryRepository: BleDiscoveryRepository
) {
    operator fun invoke(nodeId: String, address: String) {
        bleDiscoveryRepository.connect(nodeId, address)
    }
}
