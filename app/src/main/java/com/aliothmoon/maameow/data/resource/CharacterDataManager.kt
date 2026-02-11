package com.aliothmoon.maameow.data.resource

import android.os.Environment
import com.aliothmoon.maameow.data.config.MaaPathConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import java.io.File

class CharacterDataManager(
    pathConfig: MaaPathConfig
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val resourceDir = pathConfig.resourceDir

    // 干员数据缓存
    private var charactersCache: Map<String, CharacterInfo>? = null
    private var nameIndexCache: Map<String, CharacterInfo>? = null
    private var characterNamesCache: List<String>? = null
    private val characterMutex = Mutex()

    // 肉鸽核心干员缓存 (按主题)
    private val roguelikeCoreCharCache = mutableMapOf<String, List<String>>()
    private val roguelikeMutex = Mutex()

    /**
     * 检查干员名称是否有效
     * 支持多语言名称（简中/繁中/英/日/韩）
     * 空字符串视为有效（表示不指定核心干员）
     */
    suspend fun isValidCharacterName(name: String): Boolean {
        if (name.isBlank()) return true
        return getCharacterByNameOrAlias(name) != null
    }

    /**
     * 通过名称或别名查找干员
     * 大小写不敏感
     */
    suspend fun getCharacterByNameOrAlias(name: String): CharacterInfo? {
        if (name.isBlank()) return null
        val index = loadNameIndex()
        return index[name.lowercase()]
    }


    /**
     * 搜索干员名称（用于自动补全）
     * @param query 搜索关键词
     * @param limit 返回数量限制
     */
    suspend fun searchCharacters(query: String, limit: Int = 20): List<String> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()
        val index = loadNameIndex()

        // 先找精确匹配，再找包含匹配
        val exactMatch = index[lowerQuery]?.name
        val containsMatches =
            index.entries.filter { it.key.contains(lowerQuery) && it.key != lowerQuery }
                .map { it.value.name }.distinct().take(limit - if (exactMatch != null) 1 else 0)

        return if (exactMatch != null) {
            listOf(exactMatch) + containsMatches
        } else {
            containsMatches
        }
    }

    /**
     * 获取指定主题的肉鸽推荐核心干员列表
     * 从 resource/roguelike/{theme}/recruitment.json 加载
     * 只返回 is_start=true 的干员
     */
    suspend fun getRoguelikeCoreCharList(theme: String): List<String> {
        roguelikeMutex.withLock {
            roguelikeCoreCharCache[theme]?.let { return it }

            val coreChars = withContext(Dispatchers.IO) {
                try {
                    val file = File(resourceDir, "roguelike/$theme/recruitment.json")
                    if (!file.exists()) {
                        Timber.w("recruitment.json 不存在: ${file.absolutePath}")
                        return@withContext emptyList()
                    }

                    val content = file.readText()
                    parseRecruitmentJson(content)
                } catch (e: Exception) {
                    Timber.e(e, "加载 recruitment.json 失败: $theme")
                    emptyList()
                }
            }

            roguelikeCoreCharCache[theme] = coreChars
            return coreChars
        }
    }

    /**
     * 预加载干员数据
     */
    suspend fun preload() {
        loadCharacters()
        loadNameIndex()
    }


    /**
     * 加载干员数据（内部方法，不加锁）
     * 调用方必须持有 characterMutex 锁
     */
    private suspend fun loadCharactersInternal(): Map<String, CharacterInfo> {
        charactersCache?.let { return it }

        val characters = withContext(Dispatchers.IO) {
            try {
                val file = File(resourceDir, "battle_data.json")
                if (!file.exists()) {
                    Timber.w("battle_data.json 不存在: ${file.absolutePath}")
                    return@withContext emptyMap()
                }

                val content = file.readText()
                parseBattleDataJson(content)
            } catch (e: Exception) {
                Timber.e(e, "加载 battle_data.json 失败")
                emptyMap()
            }
        }

        charactersCache = characters
        return characters
    }

    private suspend fun loadCharacters(): Map<String, CharacterInfo> {
        characterMutex.withLock {
            return loadCharactersInternal()
        }
    }

    private suspend fun loadNameIndex(): Map<String, CharacterInfo> {
        nameIndexCache?.let { return it }
        characterMutex.withLock {
            nameIndexCache?.let { return it }

            // 调用不加锁的内部方法，避免死锁
            val characters = loadCharactersInternal()
            val index = buildNameIndex(characters)

            nameIndexCache = index
            return index
        }
    }

    /**
     * 构建名称索引
     * 支持多语言名称查找
     */
    private fun buildNameIndex(characters: Map<String, CharacterInfo>): Map<String, CharacterInfo> {
        val index = mutableMapOf<String, CharacterInfo>()

        for (character in characters.values) {
            // 添加各语言名称到索引
            character.name.takeIf { it.isNotBlank() }?.let {
                index[it.lowercase()] = character
            }
            character.nameEn?.takeIf { it.isNotBlank() }?.let {
                index[it.lowercase()] = character
            }
            character.nameJp?.takeIf { it.isNotBlank() }?.let {
                index[it.lowercase()] = character
            }
            character.nameKr?.takeIf { it.isNotBlank() }?.let {
                index[it.lowercase()] = character
            }
            character.nameTw?.takeIf { it.isNotBlank() }?.let {
                index[it.lowercase()] = character
            }
        }

        return index
    }

    /**
     * 解析 battle_data.json
     */
    private fun parseBattleDataJson(content: String): Map<String, CharacterInfo> {
        val jsonObject = json.parseToJsonElement(content).jsonObject
        val charsObject = jsonObject["chars"]?.jsonObject ?: return emptyMap()

        return charsObject.mapNotNull { (id, element) ->
            try {
                val charObj = element.jsonObject
                val info = CharacterInfo(
                    id = id,
                    name = charObj["name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null,
                    nameEn = charObj["name_en"]?.jsonPrimitive?.contentOrNull,
                    nameJp = charObj["name_jp"]?.jsonPrimitive?.contentOrNull,
                    nameKr = charObj["name_kr"]?.jsonPrimitive?.contentOrNull,
                    nameTw = charObj["name_tw"]?.jsonPrimitive?.contentOrNull,
                    position = charObj["position"]?.jsonPrimitive?.contentOrNull,
                    profession = charObj["profession"]?.jsonPrimitive?.contentOrNull,
                    rarity = charObj["rarity"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                )
                id to info
            } catch (e: Exception) {
                Timber.w(e, "解析干员数据失败: $id")
                null
            }
        }.toMap()
    }

    /**
     * 解析 recruitment.json
     * 提取 is_start=true 的干员名称列表
     */
    private fun parseRecruitmentJson(content: String): List<String> {
        val coreChars = mutableSetOf<String>()

        try {
            val jsonObject = json.parseToJsonElement(content).jsonObject
            val priorityArray = jsonObject["priority"]?.jsonArray ?: return emptyList()

            for (priorityItem in priorityArray) {
                val opersArray = priorityItem.jsonObject["opers"]?.jsonArray ?: continue

                for (operItem in opersArray) {
                    val operObj = operItem.jsonObject
                    val isStart = operObj["is_start"]?.jsonPrimitive?.boolean ?: false

                    if (isStart) {
                        val name = operObj["name"]?.jsonPrimitive?.contentOrNull
                        if (!name.isNullOrBlank()) {
                            coreChars.add(name)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "解析 recruitment.json 失败")
        }

        return coreChars.toList().sorted()
    }
}
