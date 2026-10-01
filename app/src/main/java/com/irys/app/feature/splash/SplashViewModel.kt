package com.irys.app.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.domain.usecases.app.GetOnboardingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        evaluateInitialRoute()
    }

    fun evaluateInitialRoute() {
        viewModelScope.launch(ioDispatcher) {
            val onboardingCompleted = getOnboardingStatusUseCase().first()
            val target = if (!onboardingCompleted) {
                SplashNavigationTarget.Onboarding
            } else {
                SplashNavigationTarget.Home
            }
            _uiState.value = SplashUiState(navigationTarget = target)
        }
    }
}
