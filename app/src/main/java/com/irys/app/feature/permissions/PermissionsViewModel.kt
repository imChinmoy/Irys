package com.irys.app.feature.permissions

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionsUiState())
    val uiState: StateFlow<PermissionsUiState> = _uiState.asStateFlow()

    fun updatePermissionStatus(isNearbyGranted: Boolean, isLocationGranted: Boolean) {
        _uiState.update { state ->
            val updatedList = state.permissions.mapIndexed { index, item ->
                when (index) {
                    0 -> item.copy(isGranted = isNearbyGranted)
                    1 -> item.copy(isGranted = isLocationGranted)
                    else -> item
                }
            }
            state.copy(
                permissions = updatedList,
                allGranted = isNearbyGranted
            )
        }
    }
}
