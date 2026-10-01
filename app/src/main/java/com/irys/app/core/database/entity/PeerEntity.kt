package com.irys.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey
    val nodeId: String,
    val alias: String,
    val lastSeenTimestamp: Long,
    val isDirect: Boolean,
    val hopDistance: Int,
    val rssi: Int = -70,
    val connectionStatus: String = "DISCOVERED"
)
