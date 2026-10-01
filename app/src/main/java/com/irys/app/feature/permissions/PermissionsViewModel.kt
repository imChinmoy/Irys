package com.irys.app.feature.permissions

import androidx.lifecycle.ViewModel
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val getPermissionsStatusUseCase: GetPermissionsStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val statuses = getPermissionsStatusUseCase()
        val allRequiredGranted = getPermissionsStatusUseCase.areRequiredGranted()

        _uiState.update { current ->
            current.copy(
                permissions = statuses.map { status ->
                    PermissionItemUiModel(
                        type = status.type,
                        title = status.title,
                        description = status.description,
                        isGranted = status.isGranted,
                        isRequired = status.isRequired
                    )
                },
                allRequiredGranted = allRequiredGranted,
                isDenied = if (allRequiredGranted) false else current.isDenied,
                isPermanentlyDenied = if (allRequiredGranted) false else current.isPermanentlyDenied,
                rationaleMessage = if (allRequiredGranted) null else current.rationaleMessage
            )
        }
    }

    fun onPermissionsResult(
        results: Map<String, Boolean>,
        shouldShowRationale: (permission: String) -> Boolean
    ) {
        val requiredPermissions = getPermissionsStatusUseCase.getRequiredPermissionsList()
        val deniedRequired = requiredPermissions.filter { perm ->
            results[perm] == false
        }

        if (deniedRequired.isEmpty()) {
            refreshPermissions()
            return
        }

        // Check if any denied permission has shouldShowRationale == false
        val permanentlyDenied = deniedRequired.any { perm ->
            !shouldShowRationale(perm)
        }

        val statuses = getPermissionsStatusUseCase()
        _uiState.update { current ->
            current.copy(
                permissions = statuses.map { status ->
                    PermissionItemUiModel(
                        type = status.type,
                        title = status.title,
                        description = status.description,
                        isGranted = status.isGranted,
                        isRequired = status.isRequired
                    )
                },
                allRequiredGranted = false,
                isDenied = !permanentlyDenied,
                isPermanentlyDenied = permanentlyDenied,
                rationaleMessage = if (permanentlyDenied) {
                    "Permissions are permanently denied. Bluetooth mesh discovery cannot operate. Please enable permissions in App Settings."
                } else {
                    "Bluetooth permissions are required for offline mesh communication. Please grant permissions to continue."
                }
            )
        }
    }

    fun retryPermissions() {
        _uiState.update { it.copy(isDenied = false) }
    }

    fun getPermissionsToRequest(): Array<String> {
        return getPermissionsStatusUseCase.getRequiredPermissionsList().toTypedArray()
    }
}
