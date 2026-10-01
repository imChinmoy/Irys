package com.irys.app.domain.usecases.app

import com.irys.app.domain.repo.AppSettingsRepository
import javax.inject.Inject

class GetNodeAliasUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(): String {
        return appSettingsRepository.getNodeAlias()
    }
}
