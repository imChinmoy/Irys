package com.irys.app.domain.usecases.app

import com.irys.app.domain.repo.AppSettingsRepository
import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(completed: Boolean = true) {
        appSettingsRepository.setOnboardingCompleted(completed)
    }
}
