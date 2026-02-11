package com.aliothmoon.maameow.remote

import android.content.pm.IPackageManager
import android.os.Build
import android.os.IDeviceIdleController
import android.provider.Settings
import com.aliothmoon.maameow.third.FakeContext
import com.aliothmoon.maameow.third.Ln
import com.android.internal.app.IAppOpsService
import rikka.shizuku.SystemServiceHelper

object PermissionGrantHelper {
    private const val TAG = "PermissionGrantHelper"
    private val packageManager: IPackageManager by lazy {
        val originalBinder = SystemServiceHelper.getSystemService("package")
        IPackageManager.Stub.asInterface(originalBinder)
    }

    private val appOpsService: IAppOpsService by lazy {
        val originalBinder = SystemServiceHelper.getSystemService("appops")
        IAppOpsService.Stub.asInterface(originalBinder)
    }

    private val deviceIdleController: IDeviceIdleController by lazy {
        val originalBinder = SystemServiceHelper.getSystemService("deviceidle")
        IDeviceIdleController.Stub.asInterface(originalBinder)
    }

    fun grantAccessibilityService(serviceId: String): Boolean {
        if (serviceId == "") {
            return false
        }
        return try {
            val contentResolver = FakeContext.get().contentResolver
            val existingServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""

            if (existingServices.contains(serviceId)) {
                Ln.i("$TAG: Accessibility service already enabled: $serviceId")
                return true
            }

            val newServices = if (existingServices.isEmpty()) {
                serviceId
            } else {
                "$existingServices:$serviceId"
            }

            Settings.Secure.putString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                newServices
            )
            Settings.Secure.putInt(
                contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                1
            )
            Ln.i("$TAG: Accessibility service enabled: $serviceId")
            true
        } catch (e: Exception) {
            Ln.e("$TAG: Failed to enable accessibility service: $e")
            false
        }
    }


    fun grantFloatingWindowPermission(packageName: String, uid: Int): Boolean {
        return try {
            // OP_SYSTEM_ALERT_WINDOW = 24
            appOpsService.setMode(24, uid, packageName, 0) // MODE_ALLOWED = 0
            Ln.i("$TAG: Floating window permission granted for $packageName")
            true
        } catch (e: Exception) {
            Ln.e("$TAG: Failed to grant floating window permission: $e")
            false
        }
    }


    fun grantNotificationPermission(packageName: String, uid: Int): Boolean {
        return try {
            // OP_POST_NOTIFICATION = 11
            appOpsService.setMode(11, uid, packageName, 0) // MODE_ALLOWED = 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runCatching {
                    packageManager.grantRuntimePermission(
                        packageName,
                        "android.permission.POST_NOTIFICATIONS",
                        0
                    )
                }.onFailure {
                    Ln.w("$TAG: Failed to grant POST_NOTIFICATIONS runtime permission: $it")
                }
            }

            Ln.i("$TAG: Notification permission granted for $packageName")
            true
        } catch (e: Exception) {
            Ln.e("$TAG: Failed to grant notification permission: $e")
            false
        }
    }


    fun grantBatteryOptimizationExemption(packageName: String): Boolean {
        return try {
            deviceIdleController.addPowerSaveWhitelistApp(packageName)
            Ln.i("$TAG: Battery optimization exemption granted for $packageName")
            true
        } catch (e: Exception) {
            Ln.e("$TAG: Failed to grant battery optimization exemption: $e")
            false
        }
    }


    fun grantStoragePermission(packageName: String, uid: Int): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                appOpsService.setMode(92, uid, packageName, 0) // MODE_ALLOWED = 0
                Ln.i("$TAG: MANAGE_EXTERNAL_STORAGE granted for $packageName via AppOps")
            } else {
                packageManager.grantRuntimePermission(
                    packageName,
                    "android.permission.READ_EXTERNAL_STORAGE",
                    0
                )
                packageManager.grantRuntimePermission(
                    packageName,
                    "android.permission.WRITE_EXTERNAL_STORAGE",
                    0
                )
                Ln.i("$TAG: Storage permission granted for $packageName")
            }
            true
        } catch (e: Exception) {
            Ln.e("$TAG: Failed to grant storage permission: $e", e)
            false
        }
    }
}