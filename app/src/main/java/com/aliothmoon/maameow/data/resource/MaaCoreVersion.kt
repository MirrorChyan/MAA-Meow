package com.aliothmoon.maameow.data.resource

/**
 * MaaCore 版本管理
 * 用于版本兼容性检查
 *
 * 目前使用硬编码版本，后续需要从 MaaCore 库或配置文件中读取
 */
object MaaCoreVersion {

    /**
     * 当前 MaaCore 版本
     * TODO: 从 MaaCore 库获取实际版本
     */
    private const val CURRENT_VERSION = "v6.0.0"

    /**
     * 是否为调试版本
     */
    private const val IS_DEBUG_VERSION = true


    /**
     * 检查当前版本是否满足最低要求
     * @param minimumRequired 最低要求版本（如 "v6.0.0-beta.1"）
     * @return true 表示满足要求，false 表示版本过低
     */
    fun meetsMinimumRequired(minimumRequired: String?): Boolean {
        // 调试版本跳过检查
        if (IS_DEBUG_VERSION) return true

        // 无版本要求
        if (minimumRequired.isNullOrBlank()) return true

        return try {
            val current = parseVersion(CURRENT_VERSION)
            val required = parseVersion(minimumRequired)
            compareVersions(current, required) >= 0
        } catch (e: Exception) {
            // 解析失败，保守起见返回 true
            true
        }
    }

    /**
     * 解析版本号
     * 支持格式：v6.0.0, v6.0.0-beta.1, 6.0.0
     */
    private fun parseVersion(version: String): VersionInfo {
        val cleanVersion = version.removePrefix("v").removePrefix("V")
        val parts = cleanVersion.split("-", limit = 2)
        val mainPart = parts[0]
        val preRelease = parts.getOrNull(1)

        val numbers = mainPart.split(".").map { it.toIntOrNull() ?: 0 }
        return VersionInfo(
            major = numbers.getOrElse(0) { 0 },
            minor = numbers.getOrElse(1) { 0 },
            patch = numbers.getOrElse(2) { 0 },
            preRelease = preRelease
        )
    }

    /**
     * 比较两个版本
     * @return 负数表示 a < b，0 表示相等，正数表示 a > b
     */
    private fun compareVersions(a: VersionInfo, b: VersionInfo): Int {
        // 比较主版本号
        if (a.major != b.major) return a.major - b.major
        if (a.minor != b.minor) return a.minor - b.minor
        if (a.patch != b.patch) return a.patch - b.patch

        // 比较预发布版本
        // 没有预发布标签的版本 > 有预发布标签的版本
        return when {
            a.preRelease == null && b.preRelease == null -> 0
            a.preRelease == null -> 1  // a 是正式版，b 是预发布
            b.preRelease == null -> -1 // a 是预发布，b 是正式版
            else -> a.preRelease.compareTo(b.preRelease)
        }
    }

    private data class VersionInfo(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val preRelease: String?
    )
}
