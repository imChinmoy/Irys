package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.BleDiscoveryRepository
import javax.inject.Inject

class StartDiscoveryUseCase @Inject constructor(
    private val bleDiscoveryRepository: BleDiscoveryRepository
) {
    operator fun invoke() {
        bleDiscoveryRepository.startDiscovery()
    }
}
