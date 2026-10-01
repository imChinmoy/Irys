package com.irys.app

import com.irys.app.domain.model.AppPermissionType
import com.irys.app.domain.model.PermissionStatus
import com.irys.app.domain.repo.PermissionRepository
import com.irys.app.domain.usecases.app.GetPermissionsStatusUseCase
import com.irys.app.feature.permissions.PermissionsViewModel
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PermissionsViewModelTest {

    private class FakePermissionRepository : PermissionRepository {
        var requiredGranted = false
        var statuses: List<PermissionStatus> = listOf(
            PermissionStatus(
                type = AppPermissionType.BLUETOOTH_NEARBY,
                title = "Nearby Devices (Bluetooth)",
                description = "Required for mesh",
                isGranted = false,
                isRequired = true
            ),
            PermissionStatus(
                type = AppPermissionType.LOCATION,
                title = "Location",
                description = "Optional",
                isGranted = false,
                isRequired = false
            )
        )

        override fun getPermissionStatuses(): List<PermissionStatus> = statuses

        override fun areRequiredPermissionsGranted(): Boolean = requiredGranted

        override fun getRequiredPermissionsList(): List<String> = listOf("android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT")
    }

    private lateinit var fakeRepo: FakePermissionRepository
    private lateinit var useCase: GetPermissionsStatusUseCase
    private lateinit var viewModel: PermissionsViewModel

    @Before
    fun setUp() {
        fakeRepo = FakePermissionRepository()
        useCase = GetPermissionsStatusUseCase(fakeRepo)
        viewModel = PermissionsViewModel(useCase)
    }

    @Test
    fun permissionsViewModel_initialStateReflectsRepository() {
        val state = viewModel.uiState.value
        assertEquals(2, state.permissions.size)
        assertFalse(state.allRequiredGranted)
        assertFalse(state.isDenied)
        assertFalse(state.isPermanentlyDenied)
    }

    @Test
    fun permissionsViewModel_allGrantedOnSuccessfulResult() {
        fakeRepo.requiredGranted = true
        fakeRepo.statuses = fakeRepo.statuses.map { it.copy(isGranted = true) }

        viewModel.onPermissionsResult(
            results = mapOf(
                "android.permission.BLUETOOTH_SCAN" to true,
                "android.permission.BLUETOOTH_CONNECT" to true
            ),
            shouldShowRationale = { true }
        )

        val state = viewModel.uiState.value
        assertTrue(state.allRequiredGranted)
        assertFalse(state.isDenied)
        assertFalse(state.isPermanentlyDenied)
    }

    @Test
    fun permissionsViewModel_deniedWithRationaleAllowed() {
        viewModel.onPermissionsResult(
            results = mapOf(
                "android.permission.BLUETOOTH_SCAN" to false,
                "android.permission.BLUETOOTH_CONNECT" to true
            ),
            shouldShowRationale = { true }
        )

        val state = viewModel.uiState.value
        assertFalse(state.allRequiredGranted)
        assertTrue(state.isDenied)
        assertFalse(state.isPermanentlyDenied)
        assertNotNull(state.rationaleMessage)
    }

    @Test
    fun permissionsViewModel_permanentlyDeniedRequiresSettings() {
        viewModel.onPermissionsResult(
            results = mapOf(
                "android.permission.BLUETOOTH_SCAN" to false,
                "android.permission.BLUETOOTH_CONNECT" to false
            ),
            shouldShowRationale = { false }
        )

        val state = viewModel.uiState.value
        assertFalse(state.allRequiredGranted)
        assertFalse(state.isDenied)
        assertTrue(state.isPermanentlyDenied)
        assertTrue(state.rationaleMessage?.contains("App Settings") == true)
    }

    @Test
    fun permissionsViewModel_retryResetsDeniedFlag() {
        viewModel.onPermissionsResult(
            results = mapOf(
                "android.permission.BLUETOOTH_SCAN" to false
            ),
            shouldShowRationale = { true }
        )
        assertTrue(viewModel.uiState.value.isDenied)

        viewModel.retryPermissions()
        assertFalse(viewModel.uiState.value.isDenied)
    }

    @Test
    fun permissionsViewModel_returnsCorrectPermissionsToRequest() {
        val toRequest = viewModel.getPermissionsToRequest()
        assertEquals(2, toRequest.size)
        assertTrue(toRequest.contains("android.permission.BLUETOOTH_SCAN"))
        assertTrue(toRequest.contains("android.permission.BLUETOOTH_CONNECT"))
    }
}
