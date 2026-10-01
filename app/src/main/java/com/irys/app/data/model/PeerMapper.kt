package com.irys.app.data.model

import com.irys.app.core.database.entity.PeerEntity
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus

fun PeerEntity.toDomain(): Peer {
    return Peer(
        nodeId = nodeId,
        alias = alias,
        lastSeenTimestamp = lastSeenTimestamp,
        isDirect = isDirect,
        hopDistance = hopDistance,
        rssi = rssi,
        connectionStatus = try {
            PeerConnectionStatus.valueOf(connectionStatus)
        } catch (e: Exception) {
            PeerConnectionStatus.DISCOVERED
        }
    )
}

fun Peer.toEntity(): PeerEntity {
    return PeerEntity(
        nodeId = nodeId,
        alias = alias,
        lastSeenTimestamp = lastSeenTimestamp,
        isDirect = isDirect,
        hopDistance = hopDistance,
        rssi = rssi,
        connectionStatus = connectionStatus.name
    )
}
