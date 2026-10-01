package com.irys.app

import com.irys.app.core.database.dao.AppSettingDao
import com.irys.app.core.database.entity.AppSettingEntity
import com.irys.app.data.local.AppSettingsLocalDataSource
import com.irys.app.data.repo.AppSettingsRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppSettingsRepositoryTest {

    private class FakeAppSettingDao : AppSettingDao {
        private val settings = mutableMapOf<String, String>()

        override fun getSettingFlow(key: String): Flow<String?> {
            return flowOf(settings[key])
        }

        override suspend fun getSetting(key: String): String? {
            return settings[key]
        }

        override suspend fun setSetting(setting: AppSettingEntity) {
            settings[setting.key] = setting.value
        }

        override suspend fun deleteSetting(key: String) {
            settings.remove(key)
        }
    }

    private lateinit var repository: AppSettingsRepositoryImpl
    private lateinit var dao: FakeAppSettingDao

    @Before
    fun setUp() {
        dao = FakeAppSettingDao()
        val localDataSource = AppSettingsLocalDataSource(dao)
        repository = AppSettingsRepositoryImpl(localDataSource)
    }

    @Test
    fun onboardingCompleted_defaultIsFalse() = runTest {
        val completed = repository.isOnboardingCompleted().first()
        assertFalse(completed)
    }

    @Test
    fun setOnboardingCompleted_persistsTrue() = runTest {
        repository.setOnboardingCompleted(true)
        val completed = repository.isOnboardingCompleted().first()
        assertTrue(completed)
    }

    @Test
    fun nodeAlias_defaultAndCustomPersisted() = runTest {
        assertEquals("Local Node", repository.getNodeAlias())
        repository.setNodeAlias("AlphaNode")
        assertEquals("AlphaNode", repository.getNodeAlias())
    }
}
