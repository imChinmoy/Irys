package com.irys.app.domain.model

enum class PeerConnectionStatus {
    DISCOVERED,
    CONNECTING,
    CONNECTED,
    READY,
    DISCONNECTED,
    ERROR
}

data class Peer(
    val nodeId: String,
    val alias: String,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isDirect: Boolean = true,
    val hopDistance: Int = 1,
    val rssi: Int = -70,
    val connectionStatus: PeerConnectionStatus = PeerConnectionStatus.DISCOVERED
)
