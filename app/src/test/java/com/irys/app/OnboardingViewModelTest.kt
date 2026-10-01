package com.irys.app

import com.irys.app.domain.repo.AppSettingsRepository
import com.irys.app.domain.usecases.app.SetOnboardingCompletedUseCase
import com.irys.app.feature.onboarding.OnboardingViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeAppSettingsRepository : AppSettingsRepository {
        var completed = false

        override fun isOnboardingCompleted(): Flow<Boolean> = MutableStateFlow(completed)

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            this.completed = completed
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
    fun onboardingViewModel_stepProgression() = runTest(testDispatcher) {
        val repo = FakeAppSettingsRepository()
        val useCase = SetOnboardingCompletedUseCase(repo)
        val viewModel = OnboardingViewModel(useCase, testDispatcher)

        assertEquals(0, viewModel.uiState.first().currentStepIndex)

        viewModel.nextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.first().currentStepIndex)

        viewModel.nextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.first().currentStepIndex)

        viewModel.nextStep()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first().isCompleted)
        assertTrue(repo.completed)
    }
}
