package com.irys.app.core.bluetooth.advertiser

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.irys.app.core.bluetooth.model.AdvertisingState
import com.irys.app.core.common.AppConstants
import com.irys.app.domain.repo.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleAdvertiser @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionRepository: PermissionRepository
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private var leAdvertiser: BluetoothLeAdvertiser? = null
    private var advertiseCallback: AdvertiseCallback? = null

    private val _advertisingState = MutableStateFlow<AdvertisingState>(AdvertisingState.Idle)
    val advertisingState: StateFlow<AdvertisingState> = _advertisingState.asStateFlow()

    fun isSupported(): Boolean {
        val adapter = bluetoothAdapter ?: return false
        return adapter.isMultipleAdvertisementSupported || adapter.bluetoothLeAdvertiser != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasPermissions(): Boolean {
        return permissionRepository.areRequiredPermissionsGranted()
    }

    @SuppressLint("MissingPermission")
    fun startAdvertising(nodeAlias: String) {
        if (_advertisingState.value is AdvertisingState.Advertising) return

        if (!isSupported()) {
            _advertisingState.value =
                AdvertisingState.Failed(-1, "BLE Peripheral Advertising is not supported on this chipset")
            return
        }

        if (!hasPermissions()) {
            _advertisingState.value =
                AdvertisingState.Failed(-2, "Bluetooth permissions missing for advertising")
            return
        }

        if (!isBluetoothEnabled()) {
            _advertisingState.value =
                AdvertisingState.Failed(-3, "Bluetooth is disabled. Please turn on Bluetooth.")
            return
        }

        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        if (advertiser == null) {
            _advertisingState.value =
                AdvertisingState.Failed(-4, "Bluetooth LE advertiser unavailable")
            return
        }
        leAdvertiser = advertiser

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val serviceUuid = ParcelUuid(AppConstants.IRYS_SERVICE_UUID)
        // Keep payload compact (<= 8 bytes) to stay safely within legacy 31-byte limit
        val aliasBytes = nodeAlias.trim().toByteArray(Charsets.UTF_8).take(8).toByteArray()

        val data = AdvertiseData.Builder()
            .addServiceUuid(serviceUuid)
            .addServiceData(serviceUuid, aliasBytes)
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                _advertisingState.value = AdvertisingState.Advertising
            }

            override fun onStartFailure(errorCode: Int) {
                val errorMsg = when (errorCode) {
                    ADVERTISE_FAILED_ALREADY_STARTED -> "Advertising already running"
                    ADVERTISE_FAILED_DATA_TOO_LARGE -> "Broadcast packet exceeds 31-byte limit"
                    ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "BLE advertising unsupported on hardware"
                    ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal hardware/driver fault"
                    ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "No BLE advertising instances remaining"
                    else -> "Advertising failure code $errorCode"
                }
                _advertisingState.value = AdvertisingState.Failed(errorCode, errorMsg)
            }
        }

        advertiseCallback = callback

        try {
            advertiser.startAdvertising(settings, data, callback)
        } catch (e: SecurityException) {
            _advertisingState.value =
                AdvertisingState.Failed(-2, "Permission revoked while starting advertisement: ${e.message}")
        } catch (e: Exception) {
            _advertisingState.value =
                AdvertisingState.Failed(-5, "Failed to start advertising: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        val callback = advertiseCallback
        val advertiser = leAdvertiser
        if (callback != null && advertiser != null) {
            try {
                advertiser.stopAdvertising(callback)
            } catch (e: SecurityException) {
                // Permissions revoked
            } catch (e: Exception) {
                // Ignore stop failure
            }
        }
        advertiseCallback = null
        leAdvertiser = null
        _advertisingState.value = AdvertisingState.Stopped
    }
}
