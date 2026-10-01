package com.irys.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.irys.app.core.common.di.IoDispatcher
import com.irys.app.core.ui.components.StatusIndicatorType
import com.irys.app.domain.usecases.app.GetNodeAliasUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getNodeAliasUseCase: GetNodeAliasUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadNodeInformation()
    }

    private fun loadNodeInformation() {
        viewModelScope.launch(ioDispatcher) {
            val alias = getNodeAliasUseCase()
            _uiState.update {
                it.copy(
                    nodeAlias = alias,
                    meshStatus = StatusIndicatorType.OFFLINE,
                    meshStatusLabel = "Ready for Phase 1+"
                )
            }
        }
    }
}
