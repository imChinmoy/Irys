package com.irys.app.core.common

import java.util.UUID

object AppConstants {
    const val DATABASE_NAME = "irys_database"
    const val PROTOCOL_VERSION = 1
    const val DEFAULT_MAX_HOPS = 7
    const val DEFAULT_TTL_HOURS = 24

    // Centralized BLE Service & Characteristic UUIDs
    val IRYS_SERVICE_UUID: UUID = UUID.fromString("00001875-0000-1000-8000-00805f9b34fb")
    val IDENTITY_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    val CONTROL_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    val MESSAGE_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a02-0000-1000-8000-00805f9b34fb")
    val MESSAGE_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a03-0000-1000-8000-00805f9b34fb")

    // Database setting keys
    const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    const val KEY_NODE_ALIAS = "node_alias"
}
