package com.aliothmoon.maameow.domain.service.update

import com.aliothmoon.maameow.data.config.MaaPathConfig
import com.aliothmoon.maameow.data.datasource.ResourceDownloader
import com.aliothmoon.maameow.data.datasource.ZipExtractor
import com.aliothmoon.maameow.data.model.update.UpdateError
import com.aliothmoon.maameow.data.model.update.UpdateProcessState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File

class ResourceUpdateHandler(
    private val downloader: ResourceDownloader,
    private val extractor: ZipExtractor,
) {
    private val _state = MutableStateFlow<UpdateProcessState>(UpdateProcessState.Idle)
    val state: StateFlow<UpdateProcessState> = _state.asStateFlow()

    /**
     * 检查资源更新
     */
    suspend fun checkUpdate(currentVersion: String, cdk: String = "") {
        _state.value = UpdateProcessState.Checking("正在检查更新...")

        when (val result = downloader.checkVersion(currentVersion, cdk)) {
            is ResourceDownloader.VersionCheckResult.UpdateAvailable -> {
                _state.value = UpdateProcessState.Available(result.info)
            }

            is ResourceDownloader.VersionCheckResult.NoUpdate -> {
                _state.value = UpdateProcessState.NoUpdate(result.currentVersion)
            }

            is ResourceDownloader.VersionCheckResult.Error -> {
                _state.value = UpdateProcessState.Failed(
                    UpdateError.fromCode(result.code, result.message)
                )
            }
        }
    }

    /**
     * 下载并安装资源更新
     */
    suspend fun downloadAndInstall(target: File, url: String): Result<Unit> {

        // 1. 下载
        _state.value = UpdateProcessState.Downloading(0, "准备下载...", 0L, 0L)

        val downloadResult = downloader.downloadToTempFile(url) { progress ->
            _state.value = UpdateProcessState.Downloading(
                progress = progress.progress,
                speed = progress.speed,
                downloaded = progress.downloaded,
                total = progress.total
            )
        }

        val tempFile = downloadResult.getOrElse { e ->
            _state.value =
                UpdateProcessState.Failed(UpdateError.NetworkError(e.message ?: "下载失败"))
            return Result.failure(e)
        }

        // 2. 解压
        _state.value = UpdateProcessState.Extracting(0, 0, 0)

        if (!target.exists()) {
            target.mkdirs()
        }

        val extractResult = extractor.extract(
            zipFile = tempFile,
            destDir = target,
            pathFilter = { entryName ->
                // 过滤并映射路径：MaaResource-main/resource/xxx -> xxx
                val name = entryName.removePrefix("MaaResource-main/")
                if (name.startsWith("resource/")) {
                    val rf = name.removePrefix("resource/")
                    rf.ifEmpty { null }
                } else {
                    null
                }
            },
            onProgress = { progress ->
                _state.value = UpdateProcessState.Extracting(
                    progress = progress.progress,
                    current = progress.current,
                    total = progress.total
                )
            }
        )

        // 清理临时文件
        tempFile.delete()

        return extractResult.fold(
            onSuccess = {
                _state.value = UpdateProcessState.Success
                Timber.i("资源更新完成")
                Result.success(Unit)
            },
            onFailure = { e ->
                _state.value = UpdateProcessState.Failed(UpdateError.UnknownError("解压失败"))
                Result.failure(e)
            }
        )
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _state.value = UpdateProcessState.Idle
    }
}
