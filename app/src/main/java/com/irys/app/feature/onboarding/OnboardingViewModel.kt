package com.irys.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.usecases.app.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun nextStep() {
        val nextIndex = _uiState.value.currentStepIndex + 1
        if (nextIndex < _uiState.value.steps.size) {
            _uiState.update { it.copy(currentStepIndex = nextIndex) }
        } else {
            completeOnboarding()
        }
    }

    fun previousStep() {
        val prevIndex = _uiState.value.currentStepIndex - 1
        if (prevIndex >= 0) {
            _uiState.update { it.copy(currentStepIndex = prevIndex) }
        }
    }

    fun skipOnboarding() {
        completeOnboarding()
    }

    fun completeOnboarding() {
        viewModelScope.launch(ioDispatcher) {
            setOnboardingCompletedUseCase(completed = true)
            _uiState.update { it.copy(isCompleted = true) }
        }
    }
}
