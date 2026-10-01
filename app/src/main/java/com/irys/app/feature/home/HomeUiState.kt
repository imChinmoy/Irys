package com.irys.app.feature.home

import com.irys.app.core.ui.components.StatusIndicatorType

data class HomeUiState(
    val nodeAlias: String = "Irys Node",
    val meshStatus: StatusIndicatorType = StatusIndicatorType.OFFLINE,
    val meshStatusLabel: String = "Mesh Inactive",
    val bluetoothEnabled: Boolean = false,
    val internetAvailable: Boolean = false,
    val nearbyPeerCount: Int = 0,
    val pendingMessageCount: Int = 0,
    val recentConversationsCount: Int = 0
)
