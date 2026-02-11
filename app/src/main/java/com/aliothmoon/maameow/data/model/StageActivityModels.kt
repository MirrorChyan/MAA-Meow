package com.aliothmoon.maameow.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * 活动关卡相关数据模型
 * 迁移自 WPF StageActivityV2.json 结构
 */

/**
 * StageActivityV2.json 根结构
 */
@Serializable
data class StageActivityRoot(
    @SerialName("Official")
    val official: OfficialStageActivity? = null
)

/**
 * Official 服务器活动数据
 */
@Serializable
data class OfficialStageActivity(
    @SerialName("sideStoryStage")
    val sideStoryStage: Map<String, SideStoryStageEntry>? = null,

    @SerialName("resourceCollection")
    val resourceCollection: ResourceCollectionInfo? = null,

    @SerialName("miniGame")
    val miniGame: List<MiniGameEntry>? = null
)

/**
 * 支线活动入口
 */
@Serializable
data class SideStoryStageEntry(
    @SerialName("MinimumRequired")
    val minimumRequired: String? = null,

    @SerialName("Activity")
    val activity: ActivityInfo? = null,

    @SerialName("Stages")
    val stages: List<ActivityStageRaw>? = null
)

/**
 * 活动基本信息
 */
@Serializable
data class ActivityInfo(
    @SerialName("Tip")
    val tip: String? = null,

    @SerialName("StageName")
    val stageName: String? = null,

    @SerialName("UtcStartTime")
    val utcStartTime: String? = null,

    @SerialName("UtcExpireTime")
    val utcExpireTime: String? = null,

    @SerialName("TimeZone")
    val timeZone: Int = 8
)

/**
 * 活动关卡原始数据
 */
@Serializable
data class ActivityStageRaw(
    @SerialName("Display")
    val display: String,

    @SerialName("Value")
    val value: String,

    @SerialName("Drop")
    val drop: String? = null
)

/**
 * 资源收集活动信息
 */
@Serializable
data class ResourceCollectionInfo(
    @SerialName("Tip")
    val tip: String? = null,

    @SerialName("UtcStartTime")
    val utcStartTime: String? = null,

    @SerialName("UtcExpireTime")
    val utcExpireTime: String? = null,

    @SerialName("TimeZone")
    val timeZone: Int = 8,

    @SerialName("IsResourceCollection")
    val isResourceCollection: Boolean = false
)

/**
 * 小游戏入口
 */
@Serializable
data class MiniGameEntry(
    @SerialName("MinimumRequired")
    val minimumRequired: String? = null,

    @SerialName("Display")
    val display: String,

    @SerialName("Value")
    val value: String,

    @SerialName("UtcStartTime")
    val utcStartTime: String? = null,

    @SerialName("UtcExpireTime")
    val utcExpireTime: String? = null,

    @SerialName("TimeZone")
    val timeZone: Int = 8
)

// ==================== UI 使用的模型 ====================

/**
 * 活动信息（UI 使用）
 * 迁移自 WPF StageActivityInfo
 */
data class StageActivityInfo(
    val name: String,              // 活动名称
    val tip: String,               // 活动提示
    val utcStartTime: Long,        // UTC 开始时间（毫秒）
    val utcExpireTime: Long,       // UTC 结束时间（毫秒）
    val isResourceCollection: Boolean = false  // 是否为资源收集活动
) {
    /**
     * 活动是否正在进行中
     */
    val isOpen: Boolean
        get() {
            val now = System.currentTimeMillis()
            return now in utcStartTime until utcExpireTime
        }

    /**
     * 活动是否已过期
     */
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= utcExpireTime

    /**
     * 活动是否尚未开始
     */
    val isPending: Boolean
        get() = System.currentTimeMillis() < utcStartTime

    /**
     * 获取剩余天数文本
     * 迁移自 WPF StageManager.GetDaysLeftText
     */
    fun getDaysLeftText(): String {
        val now = System.currentTimeMillis()
        val daysLeft = (utcExpireTime - now) / (24 * 60 * 60 * 1000)
        return if (daysLeft > 0) "${daysLeft}天" else "不足1天"
    }

    /**
     * 获取剩余天数（整数）
     */
    fun getDaysLeft(): Long {
        val now = System.currentTimeMillis()
        return (utcExpireTime - now) / (24 * 60 * 60 * 1000)
    }

    companion object {
        // 时间格式: "yyyy/MM/dd HH:mm:ss"
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

        /**
         * 解析时间字符串为 UTC 毫秒
         * @param dateStr 时间字符串 (格式: "yyyy/MM/dd HH:mm:ss")
         * @param timeZone 时区偏移（小时）
         * @return UTC 毫秒时间戳
         */
        fun parseToUtcMillis(dateStr: String?, timeZone: Int): Long {
            if (dateStr.isNullOrBlank()) return 0L
            return try {
                val localDateTime = LocalDateTime.parse(dateStr, DATE_FORMAT)
                // 将本地时间转换为 UTC：减去时区偏移
                val offset = ZoneOffset.ofHours(timeZone)
                localDateTime.toInstant(offset).toEpochMilli()
            } catch (e: Exception) {
                0L
            }
        }

        /**
         * 从 ActivityInfo 创建
         */
        fun fromActivityInfo(name: String, info: ActivityInfo): StageActivityInfo {
            return StageActivityInfo(
                name = info.stageName ?: name,
                tip = info.tip ?: "",
                utcStartTime = parseToUtcMillis(info.utcStartTime, info.timeZone),
                utcExpireTime = parseToUtcMillis(info.utcExpireTime, info.timeZone),
                isResourceCollection = false
            )
        }

        /**
         * 从 ResourceCollectionInfo 创建
         */
        fun fromResourceCollection(info: ResourceCollectionInfo): StageActivityInfo {
            return StageActivityInfo(
                name = "资源收集",
                tip = info.tip ?: "",
                utcStartTime = parseToUtcMillis(info.utcStartTime, info.timeZone),
                utcExpireTime = parseToUtcMillis(info.utcExpireTime, info.timeZone),
                isResourceCollection = true
            )
        }
    }
}

/**
 * 活动关卡（UI 使用）
 * 迁移自 WPF ActivityStage
 */
data class ActivityStage(
    val display: String,           // 显示名称 (如 "ME-8")
    val value: String,             // 关卡代码 (如 "ME-8")
    val drop: String? = null,      // 掉落物品 ID
    val activity: StageActivityInfo? = null,  // 所属活动信息
    val activityKey: String = ""   // 活动标识
) {
    /**
     * 关卡是否可用（活动进行中）
     */
    val isAvailable: Boolean
        get() = activity?.isOpen ?: true

    companion object {
        /**
         * 从原始数据创建
         */
        fun fromRaw(
            raw: ActivityStageRaw,
            activity: StageActivityInfo?,
            activityKey: String
        ): ActivityStage {
            return ActivityStage(
                display = raw.display,
                value = raw.value,
                drop = raw.drop,
                activity = activity,
                activityKey = activityKey
            )
        }
    }
}

/**
 * 小游戏（UI 使用）
 */
data class MiniGame(
    val display: String,           // 显示名称
    val value: String,             // 任务代码
    val utcStartTime: Long,        // UTC 开始时间
    val utcExpireTime: Long        // UTC 结束时间
) {
    val isOpen: Boolean
        get() {
            val now = System.currentTimeMillis()
            return now in utcStartTime until utcExpireTime
        }

    companion object {
        fun fromEntry(entry: MiniGameEntry): MiniGame {
            return MiniGame(
                display = entry.display,
                value = entry.value,
                utcStartTime = StageActivityInfo.parseToUtcMillis(
                    entry.utcStartTime,
                    entry.timeZone
                ),
                utcExpireTime = StageActivityInfo.parseToUtcMillis(
                    entry.utcExpireTime,
                    entry.timeZone
                )
            )
        }
    }
}
