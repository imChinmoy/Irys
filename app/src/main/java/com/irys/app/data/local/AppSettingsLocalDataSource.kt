package com.irys.app.data.local

import com.irys.app.core.database.dao.AppSettingDao
import com.irys.app.core.database.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsLocalDataSource @Inject constructor(
    private val appSettingDao: AppSettingDao
) {
    fun getSettingFlow(key: String): Flow<String?> = appSettingDao.getSettingFlow(key)

    suspend fun getSetting(key: String): String? = appSettingDao.getSetting(key)

    suspend fun setSetting(key: String, value: String) {
        appSettingDao.setSetting(AppSettingEntity(key = key, value = value))
    }
}
