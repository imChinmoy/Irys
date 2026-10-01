package com.irys.app.core.bluetooth.connection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import com.irys.app.core.bluetooth.model.BleConnectionState
import com.irys.app.core.common.AppConstants
import com.irys.app.domain.repo.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleConnectionManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionRepository: PermissionRepository
) {
    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    private val activeGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private val _connectionStates = MutableStateFlow<Map<String, BleConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, BleConnectionState>> = _connectionStates.asStateFlow()

    @SuppressLint("MissingPermission")
    fun connect(nodeId: String, address: String) {
        if (!permissionRepository.areRequiredPermissionsGranted()) {
            setConnectionState(nodeId, BleConnectionState.ERROR)
            return
        }

        val adapter = bluetoothAdapter ?: run {
            setConnectionState(nodeId, BleConnectionState.ERROR)
            return
        }

        if (!adapter.isEnabled) {
            setConnectionState(nodeId, BleConnectionState.ERROR)
            return
        }

        val device: BluetoothDevice = try {
            adapter.getRemoteDevice(address)
        } catch (e: Exception) {
            setConnectionState(nodeId, BleConnectionState.ERROR)
            return
        }

        setConnectionState(nodeId, BleConnectionState.CONNECTING)

        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    setConnectionState(nodeId, BleConnectionState.ERROR)
                    disconnect(nodeId)
                    return
                }

                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        setConnectionState(nodeId, BleConnectionState.CONNECTED)
                        setConnectionState(nodeId, BleConnectionState.DISCOVERING_SERVICES)
                        try {
                            gatt?.discoverServices()
                        } catch (e: SecurityException) {
                            setConnectionState(nodeId, BleConnectionState.ERROR)
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        setConnectionState(nodeId, BleConnectionState.DISCONNECTED)
                        disconnect(nodeId)
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                    val service = gatt.getService(AppConstants.IRYS_SERVICE_UUID)
                    if (service != null) {
                        setConnectionState(nodeId, BleConnectionState.READY)
                    } else {
                        // Connected to non-Irys or service discovery failed
                        setConnectionState(nodeId, BleConnectionState.ERROR)
                    }
                } else {
                    setConnectionState(nodeId, BleConnectionState.ERROR)
                }
            }
        }

        try {
            val gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            activeGatts[nodeId] = gatt
        } catch (e: SecurityException) {
            setConnectionState(nodeId, BleConnectionState.ERROR)
        } catch (e: Exception) {
            setConnectionState(nodeId, BleConnectionState.ERROR)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect(nodeId: String) {
        val gatt = activeGatts.remove(nodeId) ?: return
        try {
            gatt.disconnect()
            gatt.close()
        } catch (e: Exception) {
            // Ignore close exceptions
        }
        setConnectionState(nodeId, BleConnectionState.DISCONNECTED)
    }

    @SuppressLint("MissingPermission")
    fun disconnectAll() {
        activeGatts.keys().toList().forEach { nodeId ->
            disconnect(nodeId)
        }
        activeGatts.clear()
        _connectionStates.value = emptyMap()
    }

    private fun setConnectionState(nodeId: String, state: BleConnectionState) {
        _connectionStates.update { current ->
            current + (nodeId to state)
        }
    }

    fun getConnectionState(nodeId: String): BleConnectionState {
        return _connectionStates.value[nodeId] ?: BleConnectionState.DISCONNECTED
    }
}
