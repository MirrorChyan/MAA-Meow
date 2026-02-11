package com.aliothmoon.maameow.data.resource

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

/**
 * 关卡信息数据模型
 * 对应 stages.json 的结构
 */
@Serializable
data class StageJsonEntry(
    val stageId: String,
    val code: String,
    val apCost: Int = 0,
    val dropInfos: List<DropInfo> = emptyList()
)

@Serializable
data class DropInfo(
    val itemId: String,
    val dropType: String
)

/**
 * 材料信息数据模型
 * 对应 item_index.json 的结构
 * 注意：某些字段在 JSON 中可能为 null
 */
@Serializable
data class ItemJsonEntry(
    val name: String,
    val icon: String = "",
    val classifyType: String? = null,
    val sortId: Int = 0,
    val description: String? = null,
    val usage: String? = null
)

/**
 * 关卡信息（UI 使用）
 */
data class StageInfo(
    val stageId: String,       // 内部 ID（如 "a001_01_perm"）
    val code: String,          // 显示代码（如 "GT-1"）
    val apCost: Int = 0,       // 理智消耗
    val openDays: List<DayOfWeek> = emptyList(),  // 开放日期（空表示每天开放）
    val category: StageCategory = StageCategory.OTHER,  // 关卡分类
    val dropItems: List<String> = emptyList(),  // 掉落物品 ID 列表
    val tip: String = ""       // 关卡提示信息（迁移自 WPF TipKey）
) {
    /**
     * 获取用于 UI 显示的名称
     * 迁移自 WPF zh-cn.xaml 第740-770行的本地化映射
     */
    val displayName: String
        get() = STAGE_DISPLAY_NAMES[code] ?: code

    /**
     * 检查关卡在指定日期是否开放
     */
    fun isOpenOn(dayOfWeek: DayOfWeek): Boolean {
        return openDays.isEmpty() || dayOfWeek in openDays
    }

    /**
     * 检查关卡今天是否开放
     */
    fun isOpenToday(): Boolean {
        return isOpenOn(DayOfWeek.of(java.time.LocalDate.now().dayOfWeek.value))
    }

    companion object {
        /**
         * 关卡代码到显示名称的映射
         * 迁移自 WPF src/MaaWpfGui/Res/Localizations/zh-cn.xaml 第740-770行
         */
        private val STAGE_DISPLAY_NAMES = mapOf(
            // 主线关卡
            "1-7" to "1-7",
            "R8-11" to "R8-11",
            "12-17-HARD" to "12-17-HARD",

            // 资源本
            "CE-6" to "龙门币",
            "LS-6" to "经验",
            "CA-5" to "技能",
            "AP-5" to "红票",
            "SK-5" to "碳",

            // 芯片本
            "PR-A-1" to "奶/盾芯片",
            "PR-A-2" to "奶/盾芯片组",
            "PR-B-1" to "术/狙芯片",
            "PR-B-2" to "术/狙芯片组",
            "PR-C-1" to "先/辅芯片",
            "PR-C-2" to "先/辅芯片组",
            "PR-D-1" to "近/特芯片",
            "PR-D-2" to "近/特芯片组",

            // 剿灭模式
            "Annihilation" to "当期剿灭",
            "Chernobog@Annihilation" to "切尔诺伯格",
            "Lungmen@Annihilation" to "龙门外环",
            "LungmenOutskirts@Annihilation" to "龙门外环",
            "LungmenDowntown@Annihilation" to "龙门市区"
        )

        /**
         * 关卡提示信息映射
         * 迁移自 WPF Localizations/zh-cn.xaml 第762-770行
         */
        val STAGE_TIPS = mapOf(
            "CE-6" to "CE-6: 龙门币",
            "AP-5" to "AP-5: 红票",
            "CA-5" to "CA-5: 技能",
            "LS-6" to "LS-6: 经验",
            "SK-5" to "SK-5: 碳",
            "PR-A-1" to "PR-A-1/2: 奶&盾芯片",
            "PR-A-2" to "PR-A-1/2: 奶&盾芯片",
            "PR-B-1" to "PR-B-1/2: 术&狙芯片",
            "PR-B-2" to "PR-B-1/2: 术&狙芯片",
            "PR-C-1" to "PR-C-1/2: 先&辅芯片",
            "PR-C-2" to "PR-C-1/2: 先&辅芯片",
            "PR-D-1" to "PR-D-1/2: 近&特芯片",
            "PR-D-2" to "PR-D-1/2: 近&特芯片"
        )
    }
}

/**
 * 关卡分类
 */
enum class StageCategory(val displayName: String) {
    MAIN("主线"),              // 主线关卡
    RESOURCE_CE("龙门币"),     // CE 系列
    RESOURCE_LS("作战记录"),   // LS 系列
    RESOURCE_CA("技巧概要"),   // CA 系列
    RESOURCE_AP("采购凭证"),   // AP 系列（芯片材料）
    RESOURCE_SK("碳素"),       // SK 系列
    CHIP_PR("芯片"),           // PR 系列（芯片本）
    ANNIHILATION("剿灭"),      // 剿灭模式
    EVENT("活动"),             // 活动关卡
    OTHER("其他");             // 其他

    companion object {
        fun fromCode(code: String): StageCategory {
            return when {
                code.startsWith("CE-") -> RESOURCE_CE
                code.startsWith("LS-") -> RESOURCE_LS
                code.startsWith("CA-") -> RESOURCE_CA
                code.startsWith("AP-") -> RESOURCE_AP
                code.startsWith("SK-") -> RESOURCE_SK
                code.startsWith("PR-") -> CHIP_PR
                code == "Annihilation" || code.contains("@Annihilation") -> ANNIHILATION
                code.matches(Regex("^\\d+-\\d+$")) -> MAIN  // 如 1-7, 10-17
                code.matches(Regex("^[A-Z]{2}-\\d+$")) -> EVENT  // 如 SN-10
                else -> OTHER
            }
        }
    }
}

/**
 * 材料信息（UI 使用）
 */
data class ItemInfo(
    val id: String,            // 材料 ID（如 "30011"）
    val name: String,          // 中文名称（如 "源岩"）
    val icon: String = "",     // 图标文件名
    val sortId: Int = 0,       // 排序 ID
    val classifyType: String = ""  // 分类类型
) {
    companion object {
        /**
         * 材料排除列表（不显示在掉落选择中）
         * see ItemListHelper 的排除逻辑
         */
        val EXCLUDED_ITEMS = setOf(
            // 龙门币
            "4001",
            // 经验书
            "2001", "2002", "2003", "2004",
            // 技能书
            "3301", "3302", "3303",
            // 碳素
            "3211", "3212", "3213",
            // 家具零件
            "3141",
            // 演习券
            "3003",
            // 采购凭证
            "3213",
            // 双芯片
            "32001", "32011", "32021", "32031",
            "32041", "32051", "32061", "32071",
            // 聚合剂/双极纳米片等高级合成材料通常不直接刷
            "mod_unlock_token", "mod_update_token_1", "mod_update_token_2"
        )

        /**
         * 判断材料是否应该显示在掉落选择中
         */
        fun shouldShowInDrops(id: String, classifyType: String): Boolean {
            if (id in EXCLUDED_ITEMS) return false
            // 只显示 MATERIAL 类型
            return classifyType == "MATERIAL"
        }
    }
}

/**
 * 资源本开放日期配置
 * see StageManager.AddPermanentStages
 */
object StageOpenDays {
    private val MONDAY = DayOfWeek.MONDAY
    private val TUESDAY = DayOfWeek.TUESDAY
    private val WEDNESDAY = DayOfWeek.WEDNESDAY
    private val THURSDAY = DayOfWeek.THURSDAY
    private val FRIDAY = DayOfWeek.FRIDAY
    private val SATURDAY = DayOfWeek.SATURDAY
    private val SUNDAY = DayOfWeek.SUNDAY

    /**
     * 资源本开放日期映射
     */
    val RESOURCE_OPEN_DAYS: Map<String, List<DayOfWeek>> = mapOf(
        // 龙门币 - CE: 周二、四、六、日
        "CE" to listOf(TUESDAY, THURSDAY, SATURDAY, SUNDAY),
        // 作战记录 - LS: 每天
        "LS" to emptyList(),
        // 技巧概要 - CA: 周二、三、五、日
        "CA" to listOf(TUESDAY, WEDNESDAY, FRIDAY, SUNDAY),
        // 采购凭证/芯片材料 - AP: 周一、四、六、日
        "AP" to listOf(MONDAY, THURSDAY, SATURDAY, SUNDAY),
        // 碳素 - SK: 周一、三、五、六
        "SK" to listOf(MONDAY, WEDNESDAY, FRIDAY, SATURDAY)
    )

    /**
     * 芯片本开放日期映射
     */
    val CHIP_OPEN_DAYS: Map<String, List<DayOfWeek>> = mapOf(
        // PR-A: 重装/医疗 - 周一、四、五、日
        "PR-A" to listOf(MONDAY, THURSDAY, FRIDAY, SUNDAY),
        // PR-B: 狙击/术师 - 周一、二、五、六
        "PR-B" to listOf(MONDAY, TUESDAY, FRIDAY, SATURDAY),
        // PR-C: 先锋/辅助 - 周三、四、六、日
        "PR-C" to listOf(WEDNESDAY, THURSDAY, SATURDAY, SUNDAY),
        // PR-D: 近卫/特种 - 周二、三、六、日
        "PR-D" to listOf(TUESDAY, WEDNESDAY, SATURDAY, SUNDAY)
    )

    /**
     * 根据关卡代码获取开放日期
     */
    fun getOpenDays(code: String): List<DayOfWeek> {
        // 检查资源本
        for ((prefix, days) in RESOURCE_OPEN_DAYS) {
            if (code.startsWith("$prefix-")) {
                return days
            }
        }
        // 检查芯片本
        for ((prefix, days) in CHIP_OPEN_DAYS) {
            if (code.startsWith("$prefix-")) {
                return days
            }
        }
        // 默认每天开放
        return emptyList()
    }
}

/**
 * 常驻关卡管理器
 * 迁移自 WPF StageManager.AddPermanentStages
 *
 * WPF GUI 只显示特定的常驻关卡列表，而不是从 stages.json 读取全部关卡
 * 包含：主线关卡、资源本、芯片本、剿灭模式
 *
 * 与 WPF AddPermanentStages 保持完全一致的关卡列表：
 * - 主线：1-7, R8-11, 12-17-HARD
 * - 资源本：CE-6, AP-5, CA-5, LS-6, SK-5（只提供最高级）
 * - 芯片本：PR-A-1/2, PR-B-1/2, PR-C-1/2, PR-D-1/2
 * - 剿灭：Annihilation
 */
object PermanentStages {

    /**
     * 常驻关卡列表
     * 迁移自 WPF StageManager.cs AddPermanentStages 方法（第567-601行）
     */
    val STAGES: List<StageInfo> by lazy {
        listOf(
            // ==================== 主线关卡 ====================
            // 参考 WPF: { "1-7", new() { Display = "1-7", Value = "1-7" } }
            createStage("1-7", "1-7"),
            // 参考 WPF: { "R8-11", new() { Display = "R8-11", Value = "R8-11" } }
            createStage("R8-11", "R8-11"),
            // 参考 WPF: { "12-17-HARD", new() { Display = "12-17-HARD", Value = "12-17-HARD" } }
            createStage("12-17-HARD", "12-17-HARD"),

            // ==================== 资源本 ====================
            // 参考 WPF: { "CE-6", new("CE-6", "CETip", [...], resourceCollection) }
            // 龙门币 CE: 周二、四、六、日
            createStage(
                code = "CE-6",
                displayName = "龙门币",
                openDays = StageOpenDays.RESOURCE_OPEN_DAYS["CE"]!!,
                tip = StageInfo.STAGE_TIPS["CE-6"] ?: ""
            ),

            // 参考 WPF: { "AP-5", new("AP-5", "APTip", [...], resourceCollection) }
            // 采购凭证 AP: 周一、四、六、日
            createStage(
                code = "AP-5",
                displayName = "红票",
                openDays = StageOpenDays.RESOURCE_OPEN_DAYS["AP"]!!,
                tip = StageInfo.STAGE_TIPS["AP-5"] ?: ""
            ),

            // 参考 WPF: { "CA-5", new("CA-5", "CATip", [...], resourceCollection) }
            // 技巧概要 CA: 周二、三、五、日
            createStage(
                code = "CA-5",
                displayName = "技能",
                openDays = StageOpenDays.RESOURCE_OPEN_DAYS["CA"]!!,
                tip = StageInfo.STAGE_TIPS["CA-5"] ?: ""
            ),

            // 参考 WPF: { "LS-6", new("LS-6", "LSTip", [], resourceCollection) }
            // 作战记录 LS: 每天开放
            createStage(
                code = "LS-6",
                displayName = "经验",
                tip = StageInfo.STAGE_TIPS["LS-6"] ?: ""
            ),

            // 参考 WPF: { "SK-5", new("SK-5", "SKTip", [...], resourceCollection) }
            // 碳素 SK: 周一、三、五、六
            createStage(
                code = "SK-5",
                displayName = "碳",
                openDays = StageOpenDays.RESOURCE_OPEN_DAYS["SK"]!!,
                tip = StageInfo.STAGE_TIPS["SK-5"] ?: ""
            ),

            // ==================== 剿灭模式 ====================
            // 参考 WPF: { "Annihilation", new() { Display = LocalizationHelper.GetString("AnnihilationMode"), Value = "Annihilation" } }
            createStage("Annihilation", "当期剿灭", category = StageCategory.ANNIHILATION),

            // ==================== 芯片本 ====================
            // 参考 WPF: { "PR-A-1", new("PR-A-1", "PR-ATip", [...], resourceCollection) }
            // PR-A: 重装/医疗 - 周一、四、五、日
            createStage(
                code = "PR-A-1",
                displayName = "奶/盾芯片",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-A"]!!,
                tip = StageInfo.STAGE_TIPS["PR-A-1"] ?: ""
            ),
            createStage(
                code = "PR-A-2",
                displayName = "奶/盾芯片组",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-A"]!!,
                tip = StageInfo.STAGE_TIPS["PR-A-2"] ?: ""
            ),

            // 参考 WPF: { "PR-B-1", new("PR-B-1", "PR-BTip", [...], resourceCollection) }
            // PR-B: 狙击/术师 - 周一、二、五、六
            createStage(
                code = "PR-B-1",
                displayName = "术/狙芯片",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-B"]!!,
                tip = StageInfo.STAGE_TIPS["PR-B-1"] ?: ""
            ),
            createStage(
                code = "PR-B-2",
                displayName = "术/狙芯片组",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-B"]!!,
                tip = StageInfo.STAGE_TIPS["PR-B-2"] ?: ""
            ),

            // 参考 WPF: { "PR-C-1", new("PR-C-1", "PR-CTip", [...], resourceCollection) }
            // PR-C: 先锋/辅助 - 周三、四、六、日
            createStage(
                code = "PR-C-1",
                displayName = "先/辅芯片",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-C"]!!,
                tip = StageInfo.STAGE_TIPS["PR-C-1"] ?: ""
            ),
            createStage(
                code = "PR-C-2",
                displayName = "先/辅芯片组",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-C"]!!,
                tip = StageInfo.STAGE_TIPS["PR-C-2"] ?: ""
            ),

            // 参考 WPF: { "PR-D-1", new("PR-D-1", "PR-DTip", [...], resourceCollection) }
            // PR-D: 近卫/特种 - 周二、三、六、日
            createStage(
                code = "PR-D-1",
                displayName = "近/特芯片",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-D"]!!,
                tip = StageInfo.STAGE_TIPS["PR-D-1"] ?: ""
            ),
            createStage(
                code = "PR-D-2",
                displayName = "近/特芯片组",
                openDays = StageOpenDays.CHIP_OPEN_DAYS["PR-D"]!!,
                tip = StageInfo.STAGE_TIPS["PR-D-2"] ?: ""
            )
        )
    }

    /**
     * 获取常驻关卡列表
     * @param filterByToday 是否只返回今天开放的关卡
     */
    fun getStageList(filterByToday: Boolean = false): List<StageInfo> {
        return if (filterByToday) {
            val today = java.time.LocalDate.now().dayOfWeek
            STAGES.filter { it.isOpenOn(today) }
        } else {
            STAGES
        }
    }


    /**
     * 创建 StageInfo 辅助方法
     */
    private fun createStage(
        code: String,
        displayName: String,
        openDays: List<DayOfWeek> = emptyList(),
        category: StageCategory = StageCategory.fromCode(code),
        tip: String = ""
    ): StageInfo {
        return StageInfo(
            stageId = code,
            code = code,
            openDays = openDays,
            category = category,
            tip = tip
        )
    }
}
