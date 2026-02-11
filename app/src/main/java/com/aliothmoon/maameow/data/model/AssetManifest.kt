package com.aliothmoon.maameow.data.model

import kotlinx.serialization.Serializable

/**
 * Assets 清单 - 构建时生成
 * 包含 assets 目录下的文件列表
 */
@Serializable
data class AssetManifest(val files: List<String>)
