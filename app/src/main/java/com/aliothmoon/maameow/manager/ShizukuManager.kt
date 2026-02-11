package com.aliothmoon.maameow.manager

import android.content.pm.PackageManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import rikka.shizuku.Shizuku
import timber.log.Timber

object ShizukuManager {


    fun addBinderReceivedListener(listener: Shizuku.OnBinderReceivedListener) {
        Shizuku.addBinderReceivedListenerSticky(listener)
    }

    fun addBinderDeadListener(listener: Shizuku.OnBinderDeadListener) {
        Shizuku.addBinderDeadListener(listener)
    }


    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            Timber.e(e, "Error pinging Shizuku binder")
            false
        }
    }

    fun checkPermissionGranted(): Boolean {
        if (!isShizukuAvailable()) {
            return false
        }
        return try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    suspend fun requestPermission(timeoutMs: Long = 15_000): Boolean {
        if (!isShizukuAvailable()) return false

        if (Shizuku.isPreV11()) return true

        if (checkPermissionGranted()) {
            return true
        }

        return try {
            withTimeoutOrNull(timeoutMs) {
                callbackFlow {
                    val requestCode = (1000..9999).random()
                    val listener = Shizuku.OnRequestPermissionResultListener { code, result ->
                        if (code == requestCode) {
                            val granted = result == PackageManager.PERMISSION_GRANTED
                            Timber.d("Permission result: code=%d, granted=%s", code, granted)
                            trySend(granted)
                            close()
                        }
                    }
                    Shizuku.addRequestPermissionResultListener(listener)
                    Timber.d("Requesting Shizuku permission with code=%d", requestCode)
                    Shizuku.requestPermission(requestCode)
                    awaitClose {
                        Shizuku.removeRequestPermissionResultListener(listener)
                    }
                }.catch { e ->
                    Timber.e(e, "Error in permission request flow")
                    emit(false)
                }.first()
            }?.also { granted ->
                Timber.d("Shizuku permission %s", if (granted) "granted" else "denied")
            } ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error requesting permission")
            false
        }
    }

    suspend fun <T> requireShizukuPermissionGranted(action: suspend () -> T): T {
        val permission = requestPermission()
        if (!permission) {
            throw IllegalStateException("request shizuku permission failed")
        }
        return action()
    }
}
