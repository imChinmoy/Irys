package com.irys.app.feature.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.irys.app.core.ui.components.IrysButton
import com.irys.app.core.ui.components.IrysButtonStyle
import com.irys.app.core.ui.components.IrysCard
import com.irys.app.core.ui.components.IrysStatusBadge
import com.irys.app.core.ui.components.IrysTopAppBar
import com.irys.app.core.ui.components.StatusIndicatorType
import com.irys.app.core.ui.theme.EmergencyAmber
import com.irys.app.core.ui.theme.StatusConnecting
import com.irys.app.core.ui.theme.StatusOnline
import com.irys.app.domain.model.Peer
import com.irys.app.domain.model.PeerConnectionStatus

@Composable
fun NearbyScreen(
    onNavigateToChat: ((String) -> Unit)? = null,
    viewModel: NearbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshHardwareState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            IrysTopAppBar(
                title = "Nearby Nodes",
                actions = {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 12.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Radio & Discovery Control Panel
            IrysCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Mesh Radio Controls",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IrysButton(
                        text = if (uiState.isScanning) "Stop Scanning" else "Scan for Peers",
                        onClick = { viewModel.toggleScan() },
                        modifier = Modifier.weight(1f),
                        style = if (uiState.isScanning) IrysButtonStyle.OUTLINED else IrysButtonStyle.PRIMARY
                    )

                    IrysButton(
                        text = if (uiState.isAdvertising) "Stop Beacon" else "Broadcast Presence",
                        onClick = { viewModel.toggleAdvertising() },
                        modifier = Modifier.weight(1f),
                        style = if (uiState.isAdvertising) IrysButtonStyle.SECONDARY else IrysButtonStyle.OUTLINED
                    )
                }

                if (!uiState.isBluetoothEnabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = EmergencyAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bluetooth is disabled on your device",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmergencyAmber
                        )
                    }
                }

                if (!uiState.hasPermissions) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = EmergencyAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bluetooth discovery permissions missing",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmergencyAmber
                        )
                    }
                }

                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Discovered Peers Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Nodes (${uiState.peers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (uiState.isScanning) {
                    Text(
                        text = "Searching...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.peers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IrysCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Nodes in Range",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap 'Scan for Peers' to discover nearby Irys nodes over Bluetooth Low Energy. Nearby devices will appear automatically.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.peers, key = { it.nodeId }) { peer ->
                        PeerItem(
                            peer = peer,
                            onConnect = { viewModel.connectToPeer(peer) },
                            onDisconnect = { viewModel.disconnectPeer(peer) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerItem(
    peer: Peer,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    IrysCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = peer.alias,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${peer.rssi} dBm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (peer.isDirect) "Direct BLE Link (1 Hop)" else "${peer.hopDistance} Hops",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val statusType = when (peer.connectionStatus) {
                        PeerConnectionStatus.CONNECTED, PeerConnectionStatus.READY -> StatusIndicatorType.ONLINE
                        PeerConnectionStatus.CONNECTING -> StatusIndicatorType.CONNECTING
                        PeerConnectionStatus.DISCONNECTED, PeerConnectionStatus.DISCOVERED -> StatusIndicatorType.OFFLINE
                        PeerConnectionStatus.ERROR -> StatusIndicatorType.ERROR
                    }

                    IrysStatusBadge(
                        status = statusType,
                        label = peer.connectionStatus.name
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (peer.connectionStatus == PeerConnectionStatus.CONNECTED || peer.connectionStatus == PeerConnectionStatus.READY) {
                IrysButton(
                    text = "Disconnect",
                    onClick = onDisconnect,
                    style = IrysButtonStyle.SECONDARY
                )
            } else {
                IrysButton(
                    text = "Connect",
                    onClick = onConnect,
                    style = IrysButtonStyle.OUTLINED
                )
            }
        }
    }
}
