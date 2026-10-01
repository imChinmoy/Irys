package com.irys.app.core.bluetooth.model

sealed interface ScanState {
    data object Idle : ScanState
    data object Scanning : ScanState
    data object Stopped : ScanState
    data class Failed(val errorCode: Int, val message: String) : ScanState
}
