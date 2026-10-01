package com.irys.app.core.bluetooth.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import com.irys.app.core.bluetooth.model.DiscoveredDevice
import com.irys.app.core.bluetooth.model.ScanState
import com.irys.app.core.common.AppConstants
import com.irys.app.domain.repo.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleScanner @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionRepository: PermissionRepository
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private var leScanner: BluetoothLeScanner? = null
    private var scanCallback: ScanCallback? = null

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    private val devicesMap = ConcurrentHashMap<String, DiscoveredDevice>()
    private val _discoveredDevices = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<DiscoveredDevice>> = _discoveredDevices.asStateFlow()

    fun isSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
                bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasPermissions(): Boolean {
        return permissionRepository.areRequiredPermissionsGranted()
    }

    @SuppressLint("MissingPermission")
    fun startScan(onDeviceDiscovered: ((DiscoveredDevice) -> Unit)? = null) {
        if (_scanState.value is ScanState.Scanning) return

        if (!isSupported()) {
            _scanState.value = ScanState.Failed(-1, "Bluetooth LE hardware is not supported on this device")
            return
        }

        if (!hasPermissions()) {
            _scanState.value = ScanState.Failed(-2, "Bluetooth permissions are missing. Please grant them to scan.")
            return
        }

        if (!isBluetoothEnabled()) {
            _scanState.value = ScanState.Failed(-3, "Bluetooth is disabled. Please enable Bluetooth.")
            return
        }

        val scanner = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            _scanState.value = ScanState.Failed(-4, "Bluetooth LE scanner is currently unavailable")
            return
        }
        leScanner = scanner

        val serviceUuid = ParcelUuid(AppConstants.IRYS_SERVICE_UUID)
        val filter = ScanFilter.Builder()
            .setServiceUuid(serviceUuid)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result ?: return
                processScanResult(result, onDeviceDiscovered)
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                results?.forEach { processScanResult(it, onDeviceDiscovered) }
            }

            override fun onScanFailed(errorCode: Int) {
                val errorMessage = when (errorCode) {
                    SCAN_FAILED_ALREADY_STARTED -> "Scan already active"
                    SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed with Bluetooth stack"
                    SCAN_FAILED_FEATURE_UNSUPPORTED -> "BLE scanning mode unsupported"
                    SCAN_FAILED_INTERNAL_ERROR -> "Internal Bluetooth driver error"
                    SCAN_FAILED_SCANNING_TOO_FREQUENTLY -> "Scanning throttled by Android. Please wait a moment."
                    else -> "Scan error code $errorCode"
                }
                _scanState.value = ScanState.Failed(errorCode, errorMessage)
            }
        }

        scanCallback = callback

        try {
            // Using filters + fallback empty filter list allows matching both filtered and service-data adverts
            scanner.startScan(listOf(filter), settings, callback)
            _scanState.value = ScanState.Scanning
        } catch (e: SecurityException) {
            _scanState.value = ScanState.Failed(-2, "Permission revoked while starting scan: ${e.message}")
        } catch (e: Exception) {
            _scanState.value = ScanState.Failed(-5, "Failed to start BLE scanner: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    private fun processScanResult(
        result: ScanResult,
        onDeviceDiscovered: ((DiscoveredDevice) -> Unit)?
    ) {
        val address = result.device?.address ?: return
        val scanRecord = result.scanRecord ?: return

        val isIrys = isIrysDevice(scanRecord)
        if (!isIrys) return

        val (nodeId, alias) = extractIdentity(result, scanRecord)

        val deviceName = try {
            scanRecord.deviceName ?: result.device?.name
        } catch (e: SecurityException) {
            scanRecord.deviceName
        }

        val device = DiscoveredDevice(
            address = address,
            name = deviceName,
            nodeId = nodeId,
            alias = alias,
            rssi = result.rssi,
            lastSeenTimestamp = System.currentTimeMillis(),
            isIrysNode = true
        )

        devicesMap[address] = device
        _discoveredDevices.value = devicesMap.values.sortedByDescending { it.lastSeenTimestamp }
        onDeviceDiscovered?.invoke(device)
    }

    private fun isIrysDevice(scanRecord: ScanRecord): Boolean {
        val targetUuid = ParcelUuid(AppConstants.IRYS_SERVICE_UUID)
        val serviceUuids = scanRecord.serviceUuids
        if (serviceUuids != null && serviceUuids.contains(targetUuid)) {
            return true
        }
        if (scanRecord.getServiceData(targetUuid) != null) {
            return true
        }
        val deviceName = scanRecord.deviceName ?: ""
        return deviceName.startsWith("Irys", ignoreCase = true)
    }

    @SuppressLint("MissingPermission")
    private fun extractIdentity(result: ScanResult, scanRecord: ScanRecord): Pair<String, String> {
        val targetUuid = ParcelUuid(AppConstants.IRYS_SERVICE_UUID)
        val serviceData = scanRecord.getServiceData(targetUuid)

        if (serviceData != null && serviceData.isNotEmpty()) {
            val aliasFromData = String(serviceData, Charsets.UTF_8).trim()
            if (aliasFromData.isNotEmpty()) {
                val nodeId = "node_${result.device.address.replace(":", "").lowercase()}"
                return Pair(nodeId, aliasFromData)
            }
        }

        val name = scanRecord.deviceName ?: result.device?.name
        val alias = if (!name.isNullOrBlank()) name else "Irys Node (${result.device.address.takeLast(5)})"
        val nodeId = "node_${result.device.address.replace(":", "").lowercase()}"
        return Pair(nodeId, alias)
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        val callback = scanCallback
        val scanner = leScanner
        if (callback != null && scanner != null) {
            try {
                scanner.stopScan(callback)
            } catch (e: SecurityException) {
                // Permissions revoked
            } catch (e: Exception) {
                // Ignore scanner stop error
            }
        }
        scanCallback = null
        leScanner = null
        _scanState.value = ScanState.Stopped
    }

    fun pruneStaleDevices(timeoutMillis: Long = 30000L) {
        val cutoff = System.currentTimeMillis() - timeoutMillis
        val iterator = devicesMap.entries.iterator()
        var removed = false
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.lastSeenTimestamp < cutoff) {
                iterator.remove()
                removed = true
            }
        }
        if (removed) {
            _discoveredDevices.value = devicesMap.values.sortedByDescending { it.lastSeenTimestamp }
        }
    }

    fun clearDevices() {
        devicesMap.clear()
        _discoveredDevices.value = emptyList()
    }
}
