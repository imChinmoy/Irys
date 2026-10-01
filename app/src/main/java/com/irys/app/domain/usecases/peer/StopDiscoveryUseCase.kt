package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.BleDiscoveryRepository
import javax.inject.Inject

class StopDiscoveryUseCase @Inject constructor(
    private val bleDiscoveryRepository: BleDiscoveryRepository
) {
    operator fun invoke() {
        bleDiscoveryRepository.stopDiscovery()
    }
}
