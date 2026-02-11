package com.aliothmoon.maameow.domain.models

import com.aliothmoon.maameow.constant.DisplayMode

enum class RunMode(
    val displayName: String,
    val displayMode: Int
) {
    FOREGROUND("前台模式", DisplayMode.PRIMARY),

    BACKGROUND("后台模式", DisplayMode.BACKGROUND)
}