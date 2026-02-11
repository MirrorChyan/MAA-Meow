package com.aliothmoon.maameow.data.model.update

/**
 * 更新源类型
 */
enum class UpdateSource(
    val displayName: String,
    val type: Int
) {
    GITHUB("GitHub", 1),
    MIRROR_CHYAN("Mirror酱", 2)
}