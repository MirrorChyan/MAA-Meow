package com.aliothmoon.maameow.data.permission

/**
 * 权限状态数据类
 */
data class PermissionState(
    val shizuku: Boolean = false,
    val overlay: Boolean = false,
    val storage: Boolean = false,
    val accessibility: Boolean = false,
    val batteryWhitelist: Boolean = false,
    val notification: Boolean = false
) {

    /**
     * 是否所有权限都已授权
     */
    val allGranted: Boolean
        get() = shizuku && overlay && storage &&
                accessibility && batteryWhitelist && notification
}
