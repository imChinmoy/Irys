package com.irys.app.domain.usecases.app

import com.irys.app.domain.model.PermissionStatus
import com.irys.app.domain.repo.PermissionRepository
import javax.inject.Inject

class GetPermissionsStatusUseCase @Inject constructor(
    private val permissionRepository: PermissionRepository
) {
    operator fun invoke(): List<PermissionStatus> = permissionRepository.getPermissionStatuses()

    fun areRequiredGranted(): Boolean = permissionRepository.areRequiredPermissionsGranted()

    fun getRequiredPermissionsList(): List<String> = permissionRepository.getRequiredPermissionsList()
}
