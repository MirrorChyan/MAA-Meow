package com.aliothmoon.maameow.data.model;

/**
 * 任务类型枚举
 */
enum class TaskType(val displayName: String, val id: String) {
    WAKE_UP("开始唤醒", "WakeUp"),
    RECRUITING("自动公招", "Recruiting"),
    BASE("基建换班", "Base"),
    COMBAT("理智作战", "Combat"),
    MALL("信用收支", "Mall"),
    MISSION("领取奖励", "Mission"),
    AUTO_ROGUELIKE("自动肉鸽", "AutoRoguelike"),
    RECLAMATION("生息演算", "Reclamation");
}