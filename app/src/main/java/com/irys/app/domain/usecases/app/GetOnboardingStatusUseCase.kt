package com.irys.app.domain.usecases.app

import com.irys.app.domain.repo.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return appSettingsRepository.isOnboardingCompleted()
    }
}
