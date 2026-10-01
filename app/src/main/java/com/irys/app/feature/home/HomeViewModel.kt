package com.irys.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.core.ui.components.StatusIndicatorType
import com.irys.app.domain.usecases.app.GetNodeAliasUseCase
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import com.irys.app.domain.usecases.conversation.GetConversationCountUseCase
import com.irys.app.domain.usecases.message.GetPendingMessageCountUseCase
import com.irys.app.domain.usecases.peer.GetPeerCountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNodeAliasUseCase: GetNodeAliasUseCase,
    private val getPermissionsStatusUseCase: GetPermissionsStatusUseCase,
    private val getPendingMessageCountUseCase: GetPendingMessageCountUseCase,
    private val getConversationCountUseCase: GetConversationCountUseCase,
    private val getPeerCountUseCase: GetPeerCountUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshState()
        observeCounts()
    }

    private fun observeCounts() {
        viewModelScope.launch(ioDispatcher) {
            launch {
                getPendingMessageCountUseCase().collectLatest { count ->
                    _uiState.update { it.copy(pendingMessageCount = count) }
                }
            }
            launch {
                getConversationCountUseCase().collectLatest { count ->
                    _uiState.update { it.copy(recentConversationsCount = count) }
                }
            }
            launch {
                getPeerCountUseCase().collectLatest { count ->
                    _uiState.update { it.copy(nearbyPeerCount = count) }
                }
            }
        }
    }

    fun refreshState() {
        viewModelScope.launch(ioDispatcher) {
            val alias = getNodeAliasUseCase()
            val permissionsGranted = getPermissionsStatusUseCase.areRequiredGranted()

            val meshStatus = if (permissionsGranted) {
                StatusIndicatorType.ONLINE
            } else {
                StatusIndicatorType.DEGRADED
            }

            val meshLabel = if (permissionsGranted) {
                "Mesh Active"
            } else {
                "Degraded (No Permissions)"
            }

            _uiState.update {
                it.copy(
                    nodeAlias = alias,
                    meshStatus = meshStatus,
                    meshStatusLabel = meshLabel,
                    bluetoothPermissionsGranted = permissionsGranted
                )
            }
        }
    }
}
