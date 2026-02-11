package com.aliothmoon.maameow.data.model

import com.aliothmoon.maameow.maa.task.MaaTaskParams
import com.aliothmoon.maameow.maa.task.MaaTaskType
import com.aliothmoon.maameow.maa.task.TaskParamProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 自动肉鸽配置 - 迁移自 WPF RoguelikeSettingsUserControlModel
 */
@Serializable
data class RoguelikeConfig(
    // 基础设置
    val theme: String = "Phantom",  // 主题：Phantom/Mizuki/Sami/Sarkaz/JieGarden
    val difficulty: Int = 0,  // 难度：-1=当前, MAX_VALUE=最高, 0=最低
    val mode: String = "Exp",  // 模式：Exp/Investment/Collectible/Squad/Exploration/CLP_PDS/FindPlaytime
    val squad: String = "",  // 起始分队
    val roles: String = "稳扎稳打",  // 起始阵容
    val coreChar: String = "",  // 核心干员

    // 开局次数
    val startsCount: Int = 99999,  // 开局次数限制

    // 投资相关
    val investmentEnabled: Boolean = true,  // 启用投资
    val investCount: Int = 999,  // 投资次数上限
    val stopWhenInvestmentFull: Boolean = false,  // 投资满时停止
    val investmentWithMoreScore: Boolean = false,  // 投资模式刷更多分数

    // 助战相关
    val useSupport: Boolean = false,  // 使用助战
    val enableNonfriendSupport: Boolean = false,  // 允许非好友助战

    // 刷开局相关
    val collectibleModeSquad: String = "",  // 烧水使用分队
    val collectibleModeShopping: Boolean = false,  // 刷开局启用购物
    val startWithEliteTwo: Boolean = false,  // 凹精二核心干员
    val onlyStartWithEliteTwo: Boolean = false,  // 只凹精二不作战

    // 刷等级模式
    val stopAtFinalBoss: Boolean = false,  // 在BOSS前暂停
    val stopAtMaxLevel: Boolean = false,  // 满级后停止

    // 月度小队/深入调查
    val monthlySquadAutoIterate: Boolean = false,  // 月度小队自动切换
    val monthlySquadCheckComms: Boolean = false,  // 月度小队通讯
    val deepExplorationAutoIterate: Boolean = false,  // 深入调查自动切换

    // 界园专用
    val findPlaytimeTarget: String = "Ling",  // 目标常乐节点：Ling/Shu/Nian

    // 水月专用
    val refreshTraderWithDice: Boolean = false,  // 骰子刷新商人

    // 萨米专用
    val firstFloorFoldartal: Boolean = false,  // 凹第一层远见密文板
    val firstFloorFoldartals: String = "",  // 远见密文板列表
    val newSquad2StartingFoldartal: Boolean = false,  // 生活队凹开局密文板
    val newSquad2StartingFoldartals: String = "",  // 开局密文板列表
    val expectedCollapsalParadigms: String = "",  // 坍缩范式列表

    // 通用高级设置
    val delayAbortUntilCombatComplete: Boolean = false  // 战斗结束前延迟停止
) : TaskParamProvider {
    companion object {
        // 主题列表
        val THEME_OPTIONS = listOf(
            "Phantom" to "傀影",
            "Mizuki" to "水月",
            "Sami" to "萨米",
            "Sarkaz" to "萨卡兹",
            "JieGarden" to "界园"
        )

        // 策略模式列表
        val MODE_OPTIONS = listOf(
            "Exp" to "刷等级，尽可能稳定地打更多层数",
            "Investment" to "刷源石锭，投资完成后自动退出",
            "Collectible" to "刷开局，刷取热水壶或精二干员开局",
            "Squad" to "刷月度小队，尽可能稳定地打更多层数",
            "Exploration" to "刷深入调查，尽可能稳定地打更多层数",
            "CLP_PDS" to "刷坍缩范式，遇到非稀有坍缩范式后直接重开",
            "FindPlaytime" to "刷常乐节点，第一层进洞，找不到需要的节点就重开"
        )

        // 简短模式名称（用于下拉框）
        val MODE_SHORT_OPTIONS = listOf(
            "Exp" to "刷等级",
            "Investment" to "刷源石锭",
            "Collectible" to "刷开局",
            "Squad" to "月度小队",
            "Exploration" to "深入调查",
            "CLP_PDS" to "刷坍缩",
            "FindPlaytime" to "刷常乐"
        )

        // 职业阵容列表
        val ROLES_OPTIONS = listOf(
            "先手必胜" to "先手必胜",
            "稳扎稳打" to "稳扎稳打",
            "取长补短" to "取长补短",
            "灵活部署" to "灵活部署",
            "坚不可摧" to "坚不可摧",
            "随心所欲" to "随心所欲"
        )

        // 目标常乐节点
        val PLAYTIME_TARGET_OPTIONS = listOf(
            "Ling" to "令 - 掷地有声",
            "Shu" to "黍 - 种因得果",
            "Nian" to "年 - 三缺一"
        )

        // 获取主题最大难度
        fun getMaxDifficultyForTheme(theme: String): Int = when (theme) {
            "Phantom" -> 15
            "Mizuki" -> 18
            "Sami" -> 18
            "Sarkaz" -> 18
            "JieGarden" -> 8
            else -> 15
        }

        // 难度选项
        fun getDifficultyOptions(theme: String): List<Pair<Int, String>> {
            val maxDiff = getMaxDifficultyForTheme(theme)
            return buildList {
                add(Int.MAX_VALUE to "最高难度")
                add(-1 to "当前难度")
                for (i in maxDiff downTo 0) {
                    add(i to "难度 $i")
                }
            }
        }

        // 策略模式列表（按主题动态变化）
        fun getModeOptionsForTheme(theme: String): List<Pair<String, String>> = when (theme) {
            "JieGarden" -> listOf(
                "Exp" to "刷等级",
                "Investment" to "刷源石锭",
                "Collectible" to "刷开局",
                "FindPlaytime" to "刷常乐"
            )

            "Sami" -> listOf(
                "Exp" to "刷等级",
                "Investment" to "刷源石锭",
                "Collectible" to "刷开局",
                "Squad" to "月度小队",
                "Exploration" to "深入调查",
                "CLP_PDS" to "刷坍缩"
            )

            else -> listOf(
                "Exp" to "刷等级",
                "Investment" to "刷源石锭",
                "Collectible" to "刷开局",
                "Squad" to "月度小队",
                "Exploration" to "深入调查"
            )
        }

        // 验证模式是否对当前主题有效
        fun isModeValidForTheme(mode: String, theme: String): Boolean {
            return getModeOptionsForTheme(theme).any { it.first == mode }
        }

        // 分队列表（按主题）
        fun getSquadOptionsForTheme(theme: String): List<String> = when (theme) {
            "Phantom" -> listOf(
                "指挥分队",
                "突击战术分队",
                "堡垒战术分队",
                "远程战术分队",
                "破坏战术分队",
                "研究分队",
                "高规格分队"
            )

            "Mizuki" -> listOf(
                "指挥分队",
                "突击战术分队",
                "堡垒战术分队",
                "远程战术分队",
                "破坏战术分队",
                "研究分队",
                "高规格分队",
                "永恒狩猎分队",
                "生活至上分队",
                "科学主义分队",
                "特训分队"
            )

            "Sami" -> listOf(
                "指挥分队",
                "突击战术分队",
                "堡垒战术分队",
                "远程战术分队",
                "破坏战术分队",
                "研究分队",
                "高规格分队",
                "永恒狩猎分队",
                "生活至上分队",
                "科学主义分队",
                "特训分队"
            )

            "Sarkaz" -> listOf(
                "指挥分队",
                "突击战术分队",
                "堡垒战术分队",
                "远程战术分队",
                "破坏战术分队",
                "研究分队",
                "高规格分队",
                "永恒狩猎分队",
                "生活至上分队",
                "科学主义分队",
                "特训分队",
                "后勤分队",
                "矛头分队",
                "点刺成锭分队"
            )

            "JieGarden" -> listOf(
                "指挥分队",
                "永恒狩猎分队",
                "生活至上分队",
                "科学主义分队",
                "特训分队"
            )

            else -> listOf("指挥分队")
        }
    }

    override fun toTaskParams(): MaaTaskParams {
        val paramsJson = buildJsonObject {
            // 基础设置
            put("theme", theme)
            put("difficulty", if (difficulty == Int.MAX_VALUE) 999 else difficulty)
            put("mode", mode)
            if (squad.isNotBlank()) put("squad", squad)
            if (roles.isNotBlank()) put("roles", roles)
            if (coreChar.isNotBlank()) put("core_char", coreChar)

            // 开局次数
            put("starts_count", startsCount)

            // 投资相关
            put("investment_enabled", investmentEnabled)
            put("investments_count", investCount)
            put("stop_when_investment_full", stopWhenInvestmentFull)
            put("investment_with_more_score", investmentWithMoreScore)

            // 助战相关
            put("use_support", useSupport)
            put("use_nonfriend_support", enableNonfriendSupport)

            // 刷开局相关
            if (collectibleModeSquad.isNotBlank()) put(
                "start_with_elite_two_squad",
                collectibleModeSquad
            )
            put("start_with_elite_two", startWithEliteTwo)
            put("only_start_with_elite_two", onlyStartWithEliteTwo)

            // 刷等级模式
            put("stop_at_final_boss", stopAtFinalBoss)
            put("stop_at_max_level", stopAtMaxLevel)

            // 月度小队/深入调查
            put("squad_auto_iterate", monthlySquadAutoIterate)
            put("check_comms", monthlySquadCheckComms)
            put("exploration_auto_iterate", deepExplorationAutoIterate)

            // 界园专用
            if (theme == "JieGarden" && mode == "FindPlaytime") {
                put("target", findPlaytimeTarget)
            }

            // 水月专用
            if (theme == "Mizuki") {
                put("refresh_trader_with_dice", refreshTraderWithDice)
            }

            // 萨米专用
            if (theme == "Sami") {
                put("first_floor_foldartal", firstFloorFoldartal)
                if (firstFloorFoldartals.isNotBlank()) put(
                    "start_foldartal_list",
                    firstFloorFoldartals
                )
                put("start_with_foldartal", newSquad2StartingFoldartal)
                if (newSquad2StartingFoldartals.isNotBlank()) put(
                    "start_with_foldartal_list",
                    newSquad2StartingFoldartals
                )
                if (expectedCollapsalParadigms.isNotBlank()) put(
                    "expected_collapsal_paradigms",
                    expectedCollapsalParadigms
                )
            }

            // 通用高级设置
            put("delay_abort_until_combat_complete", delayAbortUntilCombatComplete)
        }
        return MaaTaskParams(MaaTaskType.ROGUELIKE, paramsJson.toString())
    }
}