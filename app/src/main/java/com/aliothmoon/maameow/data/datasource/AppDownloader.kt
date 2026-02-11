package com.aliothmoon.maameow.data.datasource

import android.content.Context
import com.aliothmoon.maameow.BuildConfig
import com.aliothmoon.maameow.constant.MaaApi
import com.aliothmoon.maameow.data.api.HttpClientHelper
import com.aliothmoon.maameow.data.api.await
import com.aliothmoon.maameow.data.api.model.GitHubRelease
import com.aliothmoon.maameow.data.api.model.MirrorChyanResponse
import com.aliothmoon.maameow.data.model.update.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AppDownloader(
    private val context: Context,
    private val httpClient: HttpClientHelper
) {

    sealed class VersionCheckResult {
        data class UpdateAvailable(val info: UpdateInfo) : VersionCheckResult()
        data class NoUpdate(val currentVersion: String) : VersionCheckResult()
        data class Error(val code: Int, val message: String? = null) : VersionCheckResult()
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        /**
         * 比较语义化版本号，支持 v 前缀
         * 例如 "1.0" vs "1.1", "v1.0.0" vs "v1.1.0"
         */
        fun compareVersions(v1: String, v2: String): Int {
            val normalize = { v: String ->
                v.removePrefix("v").removePrefix("V")
                    .split(".")
                    .map { it.toIntOrNull() ?: 0 }
            }
            val parts1 = normalize(v1)
            val parts2 = normalize(v2)
            val maxLen = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLen) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 != p2) return p1.compareTo(p2)
            }
            return 0
        }
    }

    /**
     * 通过 GitHub Release API 检查更新
     */
    suspend fun checkVersionFromGitHub(): VersionCheckResult {
        return try {
            val response = httpClient.get(MaaApi.APP_GITHUB_RELEASE_LATEST)
            if (!response.isSuccessful) {
                return VersionCheckResult.Error(response.code, "GitHub API 请求失败")
            }

            val release = runCatching {
                json.decodeFromString<GitHubRelease>(response.body.string())
            }.getOrElse { e ->
                return VersionCheckResult.Error(-1, "解析响应失败: ${e.message}")
            }

            val remoteVersion = release.tagName.removePrefix("v").removePrefix("V")
            val currentVersion = BuildConfig.VERSION_NAME

            if (compareVersions(currentVersion, remoteVersion) >= 0) {
                return VersionCheckResult.NoUpdate(currentVersion)
            }

            // 查找 APK asset
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                ?: return VersionCheckResult.Error(-1, "Release 中未找到 APK 文件")

            VersionCheckResult.UpdateAvailable(
                UpdateInfo(
                    version = remoteVersion,
                    downloadUrl = apkAsset.browserDownloadUrl,
                    releaseNote = release.body
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "GitHub 检查更新失败")
            VersionCheckResult.Error(-1, e.message ?: "网络错误")
        }
    }

    /**
     * 通过 MirrorChyan API 检查更新
     */
    suspend fun checkVersionFromMirrorChyan(cdk: String = ""): VersionCheckResult {
        return try {
            val currentVersion = BuildConfig.VERSION_NAME
            val response = httpClient.get(
                MaaApi.MIRROR_CHYAN_APP_RESOURCE,
                query = buildMap {
                    put("current_version", currentVersion)
                    put("user_agent", "MAA-Meow")
                    if (cdk.isNotBlank()) {
                        put("cdk", cdk)
                    }
                }
            )

            if (response.code == 500) {
                return VersionCheckResult.Error(500, "更新服务不可用")
            }

            val body = runCatching {
                json.decodeFromString<MirrorChyanResponse>(response.body.string())
            }.getOrDefault(MirrorChyanResponse.UNKNOWN_ERR)

            if (body.code != 0) {
                return VersionCheckResult.Error(body.code, body.msg)
            }

            val data = body.data ?: return VersionCheckResult.Error(-1, "数据为空")
            val remoteVersion = data.versionName

            if (remoteVersion.isEmpty() || compareVersions(currentVersion, remoteVersion) >= 0) {
                return VersionCheckResult.NoUpdate(currentVersion)
            }

            val downloadUrl = if (cdk.isNotBlank()) {
                data.url ?: return VersionCheckResult.Error(-1, "下载链接为空")
            } else {
                // 无 CDK 时回退到 GitHub Release
                return checkVersionFromGitHub()
            }

            VersionCheckResult.UpdateAvailable(
                UpdateInfo(
                    version = remoteVersion,
                    downloadUrl = downloadUrl,
                    releaseNote = data.releaseNote
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "MirrorChyan 检查 App 更新失败")
            VersionCheckResult.Error(-1, e.message ?: "网络错误")
        }
    }

    /**
     * 下载 APK 到临时文件
     */
    suspend fun downloadToTempFile(
        url: String,
        onProgress: (ResourceDownloader.DownloadProgress) -> Unit
    ): Result<File> {
        return try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.rawClient().newCall(request).await()

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}"))
            }

            val body = response.body
            val total = body.contentLength().takeIf { it > 0 } ?: 0L
            val tempFile = File(context.cacheDir, "MaaMeow-${UUID.randomUUID()}.apk")

            withContext(Dispatchers.IO) {
                BufferedOutputStream(FileOutputStream(tempFile)).use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var downloaded = 0L
                    var lastUpdateTime = System.currentTimeMillis()
                    var lastDownloaded = 0L

                    body.byteStream().use { input ->
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloaded += read

                            val now = System.currentTimeMillis()
                            if (now - lastUpdateTime >= 300) {
                                val speed = if (now > lastUpdateTime) {
                                    (downloaded - lastDownloaded) * 1000 / (now - lastUpdateTime)
                                } else 0L

                                val progress =
                                    if (total > 0) (downloaded * 100 / total).toInt() else 0

                                onProgress(
                                    ResourceDownloader.DownloadProgress(
                                        progress = progress,
                                        speed = formatSpeed(speed),
                                        downloaded = downloaded,
                                        total = total
                                    )
                                )

                                lastUpdateTime = now
                                lastDownloaded = downloaded
                            }
                        }
                    }
                }
            }

            Result.success(tempFile)
        } catch (e: Exception) {
            Timber.e(e, "下载 APK 失败")
            Result.failure(e)
        }
    }

    private fun formatSpeed(bytesPerSecond: Long): String {
        return when {
            bytesPerSecond >= 1024 * 1024 -> String.format(
                "%.1f MB/s",
                bytesPerSecond / (1024.0 * 1024)
            )
            bytesPerSecond >= 1024 -> String.format("%.1f KB/s", bytesPerSecond / 1024.0)
            else -> "$bytesPerSecond B/s"
        }
    }
}
