package com.aliothmoon.maameow.data.model

import com.aliothmoon.maameow.maa.task.MaaTaskParams
import com.aliothmoon.maameow.maa.task.MaaTaskType
import com.aliothmoon.maameow.maa.task.TaskParamProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * 生息演算配置
 */
@Serializable
data class ReclamationConfig(
    val theme: String = "Fire",  // 主题：Fire/Tales
    val mode: String = "NoSave",  // 模式：NoSave/InSave
    val toolToCraft: String = "",  // 支援道具名称
    val incrementMode: String = "Click",  // 增加方式：Click/Hold
    val maxCraftCountPerRound: Int = 15  // 单次最大组装轮数
) : TaskParamProvider {
    override fun toTaskParams(): MaaTaskParams {
        val paramsJson = buildJsonObject {
            put("theme", theme)
            put("mode", mode)
            if (toolToCraft.isNotBlank()) put("tool_to_craft", toolToCraft)
            put("increment_mode", incrementMode)
            put("num_craft_batches", maxCraftCountPerRound)
        }
        return MaaTaskParams(MaaTaskType.RECLAMATION, paramsJson.toString())
    }
}