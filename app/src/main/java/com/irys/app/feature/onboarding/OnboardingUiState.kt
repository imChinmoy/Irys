package com.irys.app.feature.onboarding

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String
)

data class OnboardingUiState(
    val currentStepIndex: Int = 0,
    val steps: List<OnboardingStep> = listOf(
        OnboardingStep(
            title = "Welcome to Irys",
            subtitle = "Resilient Offline Mesh Communication",
            description = "Irys enables direct smartphone-to-smartphone communication when cellular towers and Internet infrastructure fail."
        ),
        OnboardingStep(
            title = "Zero Internet Needed",
            subtitle = "Decentralized Device-to-Device Radio",
            description = "Using Bluetooth Low Energy, your phone communicates locally without Wi-Fi, SIM cards, or central servers."
        ),
        OnboardingStep(
            title = "Store-Carry-Forward Mesh",
            subtitle = "Nearby Devices Act as Relays",
            description = "Messages travel opportunistically. Devices carry and forward encrypted packets across physical distances until delivered."
        ),
        OnboardingStep(
            title = "Privacy & Security",
            subtitle = "Cryptographically Protected",
            description = "Every node generates its own cryptographic identity. Payloads are encrypted end-to-end so intermediate relays cannot read them."
        ),
        OnboardingStep(
            title = "Required Permissions",
            subtitle = "Bluetooth & Local Discovery",
            description = "To find nearby mesh peers and establish local links, Android requires Bluetooth permissions. Your location is never tracked or stored."
        )
    ),
    val isCompleted: Boolean = false
)
