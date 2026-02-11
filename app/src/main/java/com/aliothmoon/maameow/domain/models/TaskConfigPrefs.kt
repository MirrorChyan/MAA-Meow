package com.aliothmoon.maameow.domain.models

import com.aliothmoon.preferences.PrefKey
import com.aliothmoon.preferences.PrefSchema


@PrefSchema
data class TaskConfigPrefs(
    @PrefKey val taskList: String = "",

    @PrefKey val wakeUpConfig: String = "",

    @PrefKey val recruitConfig: String = "",

    @PrefKey val infrastConfig: String = "",

    @PrefKey val fightConfig: String = "",

    @PrefKey val mallConfig: String = "",

    @PrefKey val awardConfig: String = "",

    @PrefKey val roguelikeConfig: String = "",

    @PrefKey val reclamationConfig: String = ""
)
