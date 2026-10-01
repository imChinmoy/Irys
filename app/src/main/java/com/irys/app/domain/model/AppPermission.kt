package com.irys.app.domain.model

enum class AppPermissionType {
    BLUETOOTH_NEARBY,
    LOCATION
}

data class PermissionStatus(
    val type: AppPermissionType,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val isRequired: Boolean = true
)
