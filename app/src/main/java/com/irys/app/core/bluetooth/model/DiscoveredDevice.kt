package com.irys.app.core.bluetooth.model

data class DiscoveredDevice(
    val address: String,
    val name: String?,
    val nodeId: String,
    val alias: String,
    val rssi: Int,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val isIrysNode: Boolean = true
)
