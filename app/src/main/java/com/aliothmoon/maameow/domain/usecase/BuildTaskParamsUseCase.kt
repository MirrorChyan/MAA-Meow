package com.aliothmoon.maameow.domain.usecase

import com.aliothmoon.maameow.data.model.TaskType
import com.aliothmoon.maameow.data.preferences.TaskConfigState
import com.aliothmoon.maameow.maa.task.MaaTaskParams

class BuildTaskParamsUseCase(private val config: TaskConfigState) {
    operator fun invoke(): List<MaaTaskParams> {
        val tasks = config.taskList.value
            .filter { it.isEnabled }
            .sortedBy { it.order }

        return tasks.mapNotNull {
            when (it.toTaskType()) {
                TaskType.WAKE_UP -> config.wakeUpConfig.value.toTaskParams()
                TaskType.RECRUITING -> config.recruitConfig.value.toTaskParams()
                TaskType.BASE -> config.infrastConfig.value.toTaskParams()
                TaskType.COMBAT -> config.fightConfig.value.toTaskParams()
                TaskType.MALL -> config.mallConfig.value.toTaskParams()
                TaskType.MISSION -> config.awardConfig.value.toTaskParams()
                TaskType.AUTO_ROGUELIKE -> config.roguelikeConfig.value.toTaskParams()
                TaskType.RECLAMATION -> config.reclamationConfig.value.toTaskParams()
                null -> null
            }
        }
    }
}