package com.irys.app.feature.permissions

data class PermissionItem(
    val title: String,
    val description: String,
    val isGranted: Boolean = false
)

data class PermissionsUiState(
    val permissions: List<PermissionItem> = listOf(
        PermissionItem(
            title = "Nearby Devices (Bluetooth)",
            description = "Required to discover nearby Irys nodes and establish mesh peer-to-peer connections without Internet."
        ),
        PermissionItem(
            title = "Location (Optional / Legacy)",
            description = "Required on older Android versions for Bluetooth Low Energy beacon scanning."
        )
    ),
    val allGranted: Boolean = false
)
