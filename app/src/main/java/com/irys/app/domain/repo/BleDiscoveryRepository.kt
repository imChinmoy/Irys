package com.irys.app.domain.repo

import kotlinx.coroutines.flow.StateFlow

interface BleDiscoveryRepository {
    val isScanning: StateFlow<Boolean>
    val isAdvertising: StateFlow<Boolean>
    val scanErrorMessage: StateFlow<String?>

    fun isBleSupported(): Boolean
    fun isBluetoothEnabled(): Boolean
    fun startDiscovery()
    fun stopDiscovery()
    fun startAdvertising(alias: String)
    fun stopAdvertising()
    fun connect(nodeId: String, address: String)
    fun disconnect(nodeId: String)
    fun pruneStalePeers()
}
