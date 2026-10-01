package com.irys.app.feature.splash

sealed interface SplashNavigationTarget {
    data object Loading : SplashNavigationTarget
    data object Onboarding : SplashNavigationTarget
    data object Permissions : SplashNavigationTarget
    data object Home : SplashNavigationTarget
}

data class SplashUiState(
    val navigationTarget: SplashNavigationTarget = SplashNavigationTarget.Loading
)
