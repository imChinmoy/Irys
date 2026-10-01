package com.irys.app.domain.repo

import com.irys.app.domain.model.PermissionStatus

interface PermissionRepository {
    fun getPermissionStatuses(): List<PermissionStatus>
    fun areRequiredPermissionsGranted(): Boolean
    fun getRequiredPermissionsList(): List<String>
}
