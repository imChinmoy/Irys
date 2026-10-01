package com.irys.app.core.bluetooth.model

enum class BleConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCOVERING_SERVICES,
    READY,
    TRANSFERRING,
    DISCONNECTING,
    ERROR
}
