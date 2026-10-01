package com.irys.app.core.bluetooth.model

sealed interface AdvertisingState {
    data object Idle : AdvertisingState
    data object Advertising : AdvertisingState
    data object Stopped : AdvertisingState
    data class Failed(val errorCode: Int, val message: String) : AdvertisingState
}
