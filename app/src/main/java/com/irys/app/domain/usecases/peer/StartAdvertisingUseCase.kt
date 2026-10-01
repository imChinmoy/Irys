package com.irys.app.domain.usecases.peer

import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.repo.BleDiscoveryRepository
import javax.inject.Inject

class StartAdvertisingUseCase @Inject constructor(
    private val bleDiscoveryRepository: BleDiscoveryRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke() {
        val alias = appSettingsRepository.getNodeAlias()
        bleDiscoveryRepository.startAdvertising(alias)
    }
}
