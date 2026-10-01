package com.irys.app.domain.repo

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun getNodeAlias(): String
    suspend fun setNodeAlias(alias: String)
}
