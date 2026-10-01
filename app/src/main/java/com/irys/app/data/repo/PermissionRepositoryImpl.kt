package com.irys.app.data.repo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.irys.app.domain.model.AppPermissionType
import com.irys.app.domain.model.PermissionStatus
import com.irys.app.domain.repo.PermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : PermissionRepository {

    override fun getRequiredPermissionsList(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun areRequiredPermissionsGranted(): Boolean {
        val required = getRequiredPermissionsList()
        return required.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun getPermissionStatuses(): List<PermissionStatus> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val scanGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
            val connectGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val advertiseGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED

            val bluetoothNearbyGranted = scanGranted && connectGranted && advertiseGranted

            val fineLocationGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            listOf(
                PermissionStatus(
                    type = AppPermissionType.BLUETOOTH_NEARBY,
                    title = "Nearby Devices (Bluetooth)",
                    description = "Required to scan, advertise, and establish peer-to-peer mesh links without cellular coverage.",
                    isGranted = bluetoothNearbyGranted,
                    isRequired = true
                ),
                PermissionStatus(
                    type = AppPermissionType.LOCATION,
                    title = "Location (Optional / Legacy)",
                    description = "Used for enhanced BLE beacon positioning and backward compatibility.",
                    isGranted = fineLocationGranted,
                    isRequired = false
                )
            )
        } else {
            val locationGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            listOf(
                PermissionStatus(
                    type = AppPermissionType.LOCATION,
                    title = "Location (Bluetooth Discovery)",
                    description = "Required by Android 11 and lower to perform Bluetooth Low Energy scanning and peer discovery.",
                    isGranted = locationGranted,
                    isRequired = true
                )
            )
        }
    }
}
