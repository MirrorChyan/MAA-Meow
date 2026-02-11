package com.aliothmoon.maameow.data.model

import kotlinx.serialization.Serializable


@Serializable
data class TaskItem(
    val type: String,  // TaskType.id
    var isEnabled: Boolean = false, var order: Int = 0
) {

    fun toTaskType(): TaskType? {
        return TaskType.entries.find { it.id == type }
    }

    companion object {
        fun from(taskType: TaskType, isEnabled: Boolean = false, order: Int = 0): TaskItem {
            return TaskItem(taskType.id, isEnabled, order)
        }
    }
}














