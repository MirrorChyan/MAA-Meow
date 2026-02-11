package com.aliothmoon.maameow.data.resource

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 干员信息数据类
 * 对应 battle_data.json 中的干员数据
 */
@Serializable
data class CharacterInfo(
    val id: String,
    val name: String,              // 简中名
    @SerialName("name_en")
    val nameEn: String? = null,    // 英文名
    @SerialName("name_jp")
    val nameJp: String? = null,    // 日文名
    @SerialName("name_kr")
    val nameKr: String? = null,    // 韩文名
    @SerialName("name_tw")
    val nameTw: String? = null,    // 繁中名
    val position: String? = null,  // MELEE/RANGED
    val profession: String? = null, // 职业
    val rarity: Int = 0            // 稀有度
)