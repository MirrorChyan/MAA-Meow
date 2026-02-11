package com.aliothmoon.maameow.data.model

import kotlinx.serialization.Serializable
import com.aliothmoon.maameow.maa.task.MaaTaskParams
import com.aliothmoon.maameow.maa.task.MaaTaskType
import com.aliothmoon.maameow.maa.task.TaskParamProvider
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
/**
 * 刷理智配置
 *
 * 完整迁移自 WPF FightSettingsUserControlModel.cs
 * 包含常规设置和高级设置的全部配置项
 *
 * WPF 源文件:
 * - ViewModel: FightSettingsUserControlModel.cs (第37-1044行)
 * - View: FightSettingsUserControl.xaml (第21-488行)
 * - Model: FightTask.cs (第19-60行)
 */
@Serializable
data class FightConfig(
    // ============ 常规设置 - 理智消耗 ============

    /**
     * 是否使用理智药
     * 对应 WPF: UseMedicine (bool?)
     * 注意: 当使用源石时，此选项自动禁用
     */
    val useMedicine: Boolean = false,

    /**
     * 理智药数量
     * 对应 WPF: MedicineNumber (int, 0-999)
     * 注意: 使用源石时会自动设为 999
     */
    val medicineNumber: Int = 999,

    /**
     * 是否使用源石
     * 对应 WPF: UseStone (bool?)
     * 注意: 默认不保存此设置（AllowUseStoneSave=false）
     */
    val useStone: Boolean = false,

    /**
     * 源石数量
     * 对应 WPF: StoneNumber (int, 0-999)
     */
    val stoneNumber: Int = 0,

    // ============ 常规设置 - 战斗限制 ============

    /**
     * 是否限制战斗次数
     * 对应 WPF: HasTimesLimited (bool?)
     */
    val hasTimesLimited: Boolean = false,

    /**
     * 最大战斗次数
     * 对应 WPF: MaxTimes (int, 0-999)
     * 默认值: 5（WPF 第453行）
     */
    val maxTimes: Int = 5,

    // ============ 常规设置 - 指定掉落 ============

    /**
     * 是否指定材料掉落
     * 对应 WPF: IsSpecifiedDrops (bool?)
     */
    val isSpecifiedDrops: Boolean = false,

    /**
     * 掉落材料 ID
     * 对应 WPF: DropsItemId (string)
     * 材料列表从资源文件加载，排除特定材料（见 ViewModel 第548-564行）
     */
    val dropsItemId: String = "",

    /**
     * 掉落材料数量
     * 对应 WPF: DropsQuantity (int, 1-1145141919)
     * 默认值: 5（WPF 第644行）
     */
    val dropsQuantity: Int = 5,

    // ============ 常规设置 - 代理倍率与关卡 ============

    /**
     * 代理倍率（代理指挥倍速）
     * 对应 WPF: Series (int)
     * 选项: 0(AUTO), 6, 5, 4, 3, 2, 1, -1(不切换)
     * 默认值: 0 (AUTO)
     * 可通过 HideSeries 隐藏
     */
    val series: Int = 0,

    /**
     * 首选关卡
     * 对应 WPF: Stage1 (string)
     * 可选关卡代码（如 "CE-6"）或从关卡列表选择
     * 支持关卡代码自动转换（如 "龙门币"→"CE-6"）
     */
    val stage1: String = "",

    /**
     * 备选关卡2
     * 对应 WPF: Stage2 (string)
     * 仅在 UseAlternateStage=true 时显示
     */
    val stage2: String = "",

    /**
     * 备选关卡3
     * 对应 WPF: Stage3 (string)
     * 仅在 UseAlternateStage=true 时显示
     */
    val stage3: String = "",

    /**
     * 备选关卡4
     * 对应 WPF: Stage4 (string)
     * 仅在 UseAlternateStage=true 时显示
     */
    val stage4: String = "",

    /**
     * 剩余理智关卡
     * 对应 WPF: RemainingSanityStage (string)
     * 由 UseRemainingSanityStage 控制显示
     * 可选 "不使用" 或关卡代码
     */
    val remainingSanityStage: String = "",

    // ============ 高级设置 - 剿灭相关 ============

    /**
     * 使用自定义剿灭
     * 对应 WPF: UseCustomAnnihilation (bool)
     */
    val useCustomAnnihilation: Boolean = false,

    /**
     * 剿灭关卡选择
     * 对应 WPF: AnnihilationStage (string)
     * 选项: "Annihilation", "Chernobog@Annihilation",
     *       "LungmenOutskirts@Annihilation", "LungmenDowntown@Annihilation"
     */
    val annihilationStage: String = "Annihilation",

    // ============ 高级设置 - 战斗策略 ============

    /**
     * 博朗台模式（节省理智模式）
     * 对应 WPF: IsDrGrandet (bool)
     * Tooltip: "等待理智恢复后再开始行动，在理智即将溢出时开始作战"
     */
    val isDrGrandet: Boolean = false,

    /**
     * 自定义关卡代码
     * 对应 WPF: CustomStageCode (bool)
     * 启用后，关卡选择从下拉框变为文本输入框
     */
    val customStageCode: Boolean = false,

    /**
     * 使用备选关卡
     * 对应 WPF: UseAlternateStage (bool)
     * 启用后显示 Stage2, Stage3, Stage4
     * 与 HideUnavailableStage 互斥
     */
    val useAlternateStage: Boolean = false,

    /**
     * 使用剩余理智关卡
     * 对应 WPF: UseRemainingSanityStage (bool)
     * 默认值: true（WPF 第246行）
     * 控制常规设置中的剩余理智关卡是否显示
     */
    val useRemainingSanityStage: Boolean = true,

    // ============ 高级设置 - 保存与显示 ============

    /**
     * 允许保存源石使用
     * 对应 WPF: AllowUseStoneSave (bool)
     * 默认值: false（WPF 第728行）
     * 启用前需要弹出警告确认框（见 ViewModel 第728-754行）
     */
    val allowUseStoneSave: Boolean = false,

    /**
     * 使用即将过期的理智药
     * 对应 WPF: UseExpiringMedicine (bool)
     * 说明: 优先使用48小时内过期的理智药
     */
    val useExpiringMedicine: Boolean = false,

    /**
     * 隐藏不可用关卡
     * 对应 WPF: HideUnavailableStage (bool)
     * 默认值: true（WPF 第769行）
     * 与 UseAlternateStage 互斥
     */
    val hideUnavailableStage: Boolean = true,

    /**
     * 隐藏代理倍率选择
     * 对应 WPF: HideSeries (bool)
     * 隐藏常规设置中的代理倍率选择下拉框
     */
    val hideSeries: Boolean = false,

    /**
     * 在刷理智设置中显示自定义基建计划
     * 对应 WPF: CustomInfrastPlanShowInFightSettings (bool)
     * 控制常规设置第4行的基建计划选择是否显示（Grid Row 4）
     * 注意: 暂不迁移此功能
     */
    val customInfrastPlanShowInFightSettings: Boolean = false,

    /**
     * 游戏掉线时自动重连
     * 对应 WPF: AutoRestartOnDrop (bool)
     * 默认值: true（WPF 第806行）
     */
    val autoRestartOnDrop: Boolean = true
) : TaskParamProvider {
    /**
     * 获取实际使用的关卡
     * 对应 WPF: Stage 属性（第72-100行）
     * 根据备选关卡和关卡开放状态自动选择
     */
    fun getActiveStage(): String {
        if (stage1.isEmpty()) return ""

        // 如果不使用备选关卡，直接返回首选关卡
        if (!useAlternateStage) return stage1

        // TODO: 实现关卡开放状态检查逻辑
        // 当前简化实现: 按优先级返回非空关卡
        return listOf(stage1, stage2, stage3, stage4)
            .firstOrNull { it.isNotEmpty() } ?: stage1
    }

    /**
     * 验证配置有效性
     */
    fun isValid(): Boolean {
        // 必须至少指定一个关卡
        if (stage1.isEmpty()) return false

        // 如果指定掉落，必须选择材料
        if (isSpecifiedDrops && dropsItemId.isEmpty()) return false

        return true
    }

    override fun toTaskParams(): MaaTaskParams {
        val stage = getActiveStage()

        // 计算实际吃药数量
        val actualMedicine = when {
            useStone -> 999  // 使用源石时理智药设为 999
            useMedicine -> medicineNumber
            else -> 0
        }

        // 计算临期药品数量
        val expiringMedicine = if (useExpiringMedicine) actualMedicine else 0

        // 计算碎石数量
        val actualStone = if (useStone) stoneNumber else 0

        // 计算次数
        val actualTimes = if (hasTimesLimited) maxTimes else 999

        val paramsJson = buildJsonObject {
            put("stage", stage)
            put("medicine", actualMedicine)
            if (expiringMedicine > 0) {
                put("expiring_medicine", expiringMedicine)
            }
            put("stone", actualStone)
            put("times", actualTimes)
            put("series", series)
            if (isDrGrandet) {
                put("DrGrandet", true)
            }
            if (isSpecifiedDrops && dropsItemId.isNotBlank()) {
                put("drops", buildJsonObject {
                    put(dropsItemId, dropsQuantity)
                })
            }
        }

        return MaaTaskParams(MaaTaskType.FIGHT, paramsJson.toString())
    }
}