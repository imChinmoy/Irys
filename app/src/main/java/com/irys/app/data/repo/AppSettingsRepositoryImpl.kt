package com.irys.app.data.repo

import com.irys.app.core.common.AppConstants
import com.irys.app.data.local.AppSettingsLocalDataSource
import com.irys.app.domain.repo.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: AppSettingsLocalDataSource
) : AppSettingsRepository {

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return localDataSource.getSettingFlow(AppConstants.KEY_ONBOARDING_COMPLETED).map {
            it == "true"
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localDataSource.setSetting(AppConstants.KEY_ONBOARDING_COMPLETED, completed.toString())
    }

    override suspend fun getNodeAlias(): String {
        return localDataSource.getSetting(AppConstants.KEY_NODE_ALIAS) ?: "Local Node"
    }

    override suspend fun setNodeAlias(alias: String) {
        localDataSource.setSetting(AppConstants.KEY_NODE_ALIAS, alias)
    }
}
