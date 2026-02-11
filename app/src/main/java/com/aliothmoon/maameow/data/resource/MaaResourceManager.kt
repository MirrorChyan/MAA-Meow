package com.aliothmoon.maameow.data.resource

import android.content.Context
import com.aliothmoon.maameow.data.api.MaaApiService
import com.aliothmoon.maameow.data.model.ActivityStage
import com.aliothmoon.maameow.data.model.MiniGame
import com.aliothmoon.maameow.data.model.StageActivityInfo
import com.aliothmoon.maameow.data.model.StageActivityRoot
import com.aliothmoon.maameow.data.config.MaaPathConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.time.LocalDate

/**
 * MAA 资源管理器
 * 负责加载和缓存 stages.json、item_index.json 以及活动关卡数据
 * 迁移自 WPF StageManager
 */
class MaaResourceManager(
    private val maaApiService: MaaApiService,
    private val pathConfig: MaaPathConfig
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** 资源目录路径 */
    val resourceDir: String
        get() = pathConfig.resourceDir

    // 缓存
    private var stageCache: List<StageInfo>? = null
    private var itemCache: Map<String, ItemInfo>? = null
    private var activityStagesCache: List<ActivityStage>? = null
    private var miniGamesCache: List<MiniGame>? = null
    private var resourceCollectionCache: StageActivityInfo? = null
    private val stageMutex = Mutex()
    private val itemMutex = Mutex()
    private val activityMutex = Mutex()

    // 资源是否已加载
    val isResourceLoaded: Boolean
        get() = stageCache != null && itemCache != null

    /**
     * 获取关卡列表
     * @param filterByToday 是否只返回今天开放的关卡
     * @param category 按分类过滤（null 表示不过滤）
     */
    suspend fun getStageList(
        filterByToday: Boolean = false,
        category: StageCategory? = null
    ): List<StageInfo> {
        val stages = loadStages()
        val today = if (filterByToday) LocalDate.now().dayOfWeek else null

        return stages.filter { stage ->
            val matchCategory = category == null || stage.category == category
            val matchDay = today == null || stage.isOpenOn(today)
            matchCategory && matchDay
        }
    }

    /**
     * 获取今日开放的资源本
     */
    suspend fun getTodayResourceStages(): List<StageInfo> {
        val stages = loadStages()
        val today = LocalDate.now().dayOfWeek

        return stages.filter { stage ->
            val isResource = stage.category in listOf(
                StageCategory.RESOURCE_CE,
                StageCategory.RESOURCE_LS,
                StageCategory.RESOURCE_CA,
                StageCategory.RESOURCE_AP,
                StageCategory.RESOURCE_SK,
                StageCategory.CHIP_PR
            )
            isResource && stage.isOpenOn(today)
        }
    }

    /**
     * 获取材料名称
     * @param itemId 材料 ID
     * @return 中文名称，找不到则返回原 ID
     */
    suspend fun getItemName(itemId: String): String {
        val items = loadItems()
        return items[itemId]?.name ?: itemId
    }

    /**
     * 获取材料信息
     */
    suspend fun getItemInfo(itemId: String): ItemInfo? {
        val items = loadItems()
        return items[itemId]
    }

    /**
     * 获取可选材料列表（用于掉落选择）
     */
    suspend fun getDropItems(): List<ItemInfo> {
        val items = loadItems()
        return items.values
            .filter { ItemInfo.shouldShowInDrops(it.id, it.classifyType) }
            .sortedBy { it.sortId }
    }

    /**
     * 批量获取材料名称
     */
    suspend fun getItemNames(itemIds: List<String>): Map<String, String> {
        val items = loadItems()
        return itemIds.associateWith { id -> items[id]?.name ?: id }
    }

    /**
     * 按分类获取关卡
     */
    suspend fun getStagesByCategory(): Map<StageCategory, List<StageInfo>> {
        return loadStages().groupBy { it.category }
    }

    /**
     * 搜索关卡
     * @param query 搜索关键词（匹配代码或 stageId）
     */
    suspend fun searchStages(query: String): List<StageInfo> {
        if (query.isBlank()) return emptyList()
        val lowerQuery = query.lowercase()
        return loadStages().filter {
            it.code.lowercase().contains(lowerQuery) ||
                    it.stageId.lowercase().contains(lowerQuery)
        }
    }

    /**
     * 预加载资源
     */
    suspend fun preload() {
        loadStages()
        loadItems()
        loadActivityStages()
    }


    // ==================== 内部方法 ====================

    private suspend fun loadStages(): List<StageInfo> {
        stageMutex.withLock {
            stageCache?.let { return it }

            val stages = withContext(Dispatchers.IO) {
                try {
                    val file = File(resourceDir, "stages.json")
                    if (!file.exists()) {
                        Timber.w("stages.json 不存在: ${file.absolutePath}")
                        return@withContext emptyList()
                    }

                    val content = file.readText()
                    // stages.json 是数组格式，不是对象格式
                    val entries: List<StageJsonEntry> = json.decodeFromString(content)

                    entries.map { entry ->
                        StageInfo(
                            stageId = entry.stageId,
                            code = entry.code,
                            apCost = entry.apCost,
                            openDays = StageOpenDays.getOpenDays(entry.code),
                            category = StageCategory.fromCode(entry.code),
                            dropItems = entry.dropInfos.map { it.itemId }
                        )
                    }.sortedBy { it.code }
                } catch (e: Exception) {
                    Timber.e(e, "加载 stages.json 失败")
                    emptyList()
                }
            }

            stageCache = stages
            Timber.d("加载了 ${stages.size} 个关卡")
            return stages
        }
    }

    private suspend fun loadItems(): Map<String, ItemInfo> {
        itemMutex.withLock {
            itemCache?.let { return it }

            val items = withContext(Dispatchers.IO) {
                try {
                    val file = File(resourceDir, "item_index.json")
                    if (!file.exists()) {
                        Timber.w("item_index.json 不存在: ${file.absolutePath}")
                        return@withContext emptyMap()
                    }

                    val content = file.readText()
                    val entries: Map<String, ItemJsonEntry> = json.decodeFromString(content)

                    entries.mapValues { (id, entry) ->
                        ItemInfo(
                            id = id,
                            name = entry.name,
                            icon = entry.icon,
                            sortId = entry.sortId,
                            classifyType = entry.classifyType ?: ""
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "加载 item_index.json 失败")
                    emptyMap()
                }
            }

            itemCache = items
            Timber.d("加载了 ${items.size} 个材料")
            return items
        }
    }

    // ==================== 活动关卡相关 ====================

    /**
     * 获取活动关卡列表
     * @param onlyOpen 是否只返回正在开放的活动关卡
     */
    suspend fun getActivityStages(onlyOpen: Boolean = true): List<ActivityStage> {
        loadActivityStages()
        val stages = activityStagesCache ?: return emptyList()
        return if (onlyOpen) {
            stages.filter { it.isAvailable }
        } else {
            stages
        }
    }

    /**
     * 获取小游戏列表
     * @param onlyOpen 是否只返回正在开放的小游戏
     */
    suspend fun getMiniGames(onlyOpen: Boolean = true): List<MiniGame> {
        loadActivityStages()
        val games = miniGamesCache ?: return emptyList()
        return if (onlyOpen) {
            games.filter { it.isOpen }
        } else {
            games
        }
    }

    /**
     * 获取资源收集活动信息
     * @return 如果资源收集活动正在进行，返回活动信息；否则返回 null
     */
    suspend fun getResourceCollection(): StageActivityInfo? {
        loadActivityStages()
        return resourceCollectionCache?.takeIf { it.isOpen }
    }

    /**
     * 刷新活动关卡数据（从网络重新加载）
     */
    suspend fun refreshActivityStages() {
        activityMutex.withLock {
            activityStagesCache = null
            miniGamesCache = null
            resourceCollectionCache = null
        }
        loadActivityStages()
    }

    /**
     * 从网络加载活动关卡数据
     * 迁移自 WPF LoadWebStages
     */
    private suspend fun loadActivityStages() {
        activityMutex.withLock {
            // 已有缓存，跳过
            if (activityStagesCache != null) return

            val jsonContent = maaApiService.getStageActivity()
            if (jsonContent == null) {
                Timber.w("无法获取活动关卡数据")
                activityStagesCache = emptyList()
                miniGamesCache = emptyList()
                return
            }

            try {
                parseActivityStages(jsonContent)
            } catch (e: Exception) {
                Timber.e(e, "解析活动关卡数据失败")
                activityStagesCache = emptyList()
                miniGamesCache = emptyList()
            }
        }
    }

    /**
     * 解析活动关卡 JSON
     * from WPF ParseActivityStages
     */
    private fun parseActivityStages(jsonContent: String) {
        val root = json.decodeFromString<StageActivityRoot>(jsonContent)
        val official = root.official

        if (official == null) {
            Timber.w("活动关卡数据中没有 Official 字段")
            activityStagesCache = emptyList()
            miniGamesCache = emptyList()
            return
        }

        val activityStages = mutableListOf<ActivityStage>()

        // 解析支线活动
        // TODO: 需要支持（YoStarJP/YoStarKR/YoStarEN），当前只解析 Official
        official.sideStoryStage?.forEach { (key, entry) ->
            // 版本兼容性检查
            if (!MaaCoreVersion.meetsMinimumRequired(entry.minimumRequired)) {
                Timber.d("跳过活动 $key: 需要版本 ${entry.minimumRequired}")
                return@forEach
            }

            val activityInfo = entry.activity?.let { info ->
                StageActivityInfo.fromActivityInfo(key, info)
            }

            entry.stages?.forEach { stageRaw ->
                activityStages.add(
                    ActivityStage.fromRaw(stageRaw, activityInfo, key)
                )
            }
        }

        // 解析小游戏
        val miniGames = official.miniGame?.map { entry ->
            MiniGame.fromEntry(entry)
        } ?: emptyList()

        // 解析资源收集活动
        val resourceCollection = official.resourceCollection?.let { info ->
            StageActivityInfo.fromResourceCollection(info)
        }

        activityStagesCache = activityStages
        miniGamesCache = miniGames
        resourceCollectionCache = resourceCollection

        Timber.d("加载了 ${activityStages.size} 个活动关卡, ${miniGames.size} 个小游戏")
        if (resourceCollection?.isOpen == true) {
            Timber.d("资源收集活动进行中: ${resourceCollection.tip}")
        }
    }

    // ==================== 合并关卡列表 ====================

    /**
     * 关卡分组数据
     * 用于 UI 显示分组标题
     */
    data class StageGroup(
        val title: String,           // 分组标题
        val stages: List<StageItem>, // 关卡列表
        val daysLeftText: String? = null  // 剩余天数文本（活动关卡分组）
    )

    /**
     * 统一的关卡项（活动关卡和常驻关卡的统一表示）
     */
    data class StageItem(
        val code: String,            // 关卡代码（如 "ME-8", "CE-6"）
        val displayName: String,     // 显示名称
        val isActivityStage: Boolean = false,  // 是否为活动关卡
        val isOpenToday: Boolean = true,       // 今天是否开放
        val drop: String? = null     // 掉落物品 ID（活动关卡）
    )

    /**
     * 获取合并后的关卡列表（按分组）
     * 迁移自 WPF MergePermanentAndActivityStages
     *
     * @param filterByToday 是否只返回今天开放的关卡
     * @return 分组后的关卡列表（活动关卡在前，常驻关卡在后）
     */
    suspend fun getMergedStageGroups(filterByToday: Boolean = false): List<StageGroup> {
        loadActivityStages()

        val groups = mutableListOf<StageGroup>()
        val today = java.time.LocalDate.now().dayOfWeek
        val isResourceCollectionOpen = resourceCollectionCache?.isOpen == true

        // 1. 活动关卡分组
        val activityStages = activityStagesCache ?: emptyList()
        val openActivityStages = activityStages.filter { it.isAvailable }

        if (openActivityStages.isNotEmpty()) {
            // 按活动分组
            val activityGroups = openActivityStages.groupBy { it.activityKey }
            activityGroups.forEach { (activityKey, stages) ->
                val activityInfo = stages.firstOrNull()?.activity
                val activityTip = activityInfo?.tip ?: activityKey
                val daysLeftText = activityInfo?.getDaysLeftText()
                val stageItems = stages.map { stage ->
                    StageItem(
                        code = stage.value,
                        displayName = stage.display,
                        isActivityStage = true,
                        isOpenToday = true,
                        drop = stage.drop
                    )
                }
                if (stageItems.isNotEmpty()) {
                    groups.add(
                        StageGroup(
                            title = activityTip,
                            stages = stageItems,
                            daysLeftText = daysLeftText
                        )
                    )
                }
            }
        }

        // 2. 常驻关卡分组
        // 首先添加「当前/上次」选项（对应 WPF DefaultStage）
        // 迁移自 WPF StageManager.InitializeDefaultStages
        // Value = "" 时，MaaCore 会使用游戏内「前往上一次作战」功能
        val defaultStageItem = StageItem(
            code = "",
            displayName = "当前/上次",
            isActivityStage = false,
            isOpenToday = true
        )

        val permanentStages = listOf(defaultStageItem) + PermanentStages.STAGES.map { stage ->
            val isOpen = if (isResourceCollectionOpen && isResourceStage(stage.code)) {
                // 资源收集活动期间，资源本每天开放
                true
            } else {
                stage.isOpenOn(today)
            }
            StageItem(
                code = stage.code,
                displayName = stage.displayName,
                isActivityStage = false,
                isOpenToday = isOpen
            )
        }

        val filteredPermanent = if (filterByToday) {
            permanentStages.filter { it.isOpenToday }
        } else {
            permanentStages
        }

        if (filteredPermanent.isNotEmpty()) {
            groups.add(StageGroup(title = "常驻关卡", stages = filteredPermanent))
        }

        return groups
    }

    /**
     * 获取合并后的扁平关卡列表（不含分组信息）
     *
     * @param filterByToday 是否只返回今天开放的关卡
     * @return 关卡列表（活动关卡在前，常驻关卡在后）
     */
    suspend fun getMergedStageList(filterByToday: Boolean = false): List<StageItem> {
        return getMergedStageGroups(filterByToday).flatMap { it.stages }
    }

    /**
     * 判断是否为资源本（受资源收集活动影响）
     */
    private fun isResourceStage(code: String): Boolean {
        return code.startsWith("CE-") ||
                code.startsWith("LS-") ||
                code.startsWith("CA-") ||
                code.startsWith("AP-") ||
                code.startsWith("SK-") ||
                code.startsWith("PR-")
    }

    /**
     * 资源收集活动是否开放
     */
    suspend fun isResourceCollectionOpen(): Boolean {
        loadActivityStages()
        return resourceCollectionCache?.isOpen == true
    }
}
