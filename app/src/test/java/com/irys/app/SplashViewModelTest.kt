package com.irys.app

import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.usecases.app.GetOnboardingStatusUseCase
import com.irys.app.feature.splash.SplashNavigationTarget
import com.irys.app.feature.splash.SplashViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAppSettingsRepository(initialOnboarding: Boolean) : AppSettingsRepository {
        private val onboardingFlow = MutableStateFlow(initialOnboarding)

        override fun isOnboardingCompleted(): Flow<Boolean> = onboardingFlow

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            onboardingFlow.value = completed
        }

        override suspend fun getNodeAlias(): String = "TestNode"

        override suspend fun setNodeAlias(alias: String) {}
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun splashViewModel_routesToOnboarding_whenNotCompleted() = runTest(testDispatcher) {
        val repo = FakeAppSettingsRepository(initialOnboarding = false)
        val useCase = GetOnboardingStatusUseCase(repo)
        val viewModel = SplashViewModel(useCase, testDispatcher)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(SplashNavigationTarget.Onboarding, state.navigationTarget)
    }

    @Test
    fun splashViewModel_routesToHome_whenOnboardingCompleted() = runTest(testDispatcher) {
        val repo = FakeAppSettingsRepository(initialOnboarding = true)
        val useCase = GetOnboardingStatusUseCase(repo)
        val viewModel = SplashViewModel(useCase, testDispatcher)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertEquals(SplashNavigationTarget.Home, state.navigationTarget)
    }
}
