package com.irys.app.feature.onboarding

data class OnboardingStep(
    val title: String,
    val description: String
)

data class OnboardingUiState(
    val currentStepIndex: Int = 0,
    val steps: List<OnboardingStep> = listOf(
        OnboardingStep(
            title = "Decentralized Communication",
            description = "Irys enables direct device-to-device communication when internet and cellular networks are unavailable."
        ),
        OnboardingStep(
            title = "Store-Carry-Forward Mesh",
            description = "Nearby smartphones act as relays. Messages travel opportunistically across devices to reach their destination."
        ),
        OnboardingStep(
            title = "Privacy & Disaster Resilience",
            description = "All messages are cryptographically secured and prioritized for emergency response."
        )
    ),
    val isCompleted: Boolean = false
)
