package com.irys.app.feature.permissions

import com.irys.app.domain.model.AppPermissionType

data class PermissionItemUiModel(
    val type: AppPermissionType,
    val title: String,
    val description: String,
    val isGranted: Boolean = false,
    val isRequired: Boolean = true
)

data class PermissionsUiState(
    val permissions: List<PermissionItemUiModel> = emptyList(),
    val allRequiredGranted: Boolean = false,
    val isDenied: Boolean = false,
    val isPermanentlyDenied: Boolean = false,
    val rationaleMessage: String? = null
)
