package com.aliothmoon.maameow.maa.task

/**
 * MaaCore#AsstAppendTask
 *
 * - type: 任务类型字符串
 * - params: 任务参数 JSON 字符串
 */
data class MaaTaskParams(
    val type: MaaTaskType,
    val params: String
)
