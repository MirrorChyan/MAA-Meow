package com.aliothmoon.maameow.domain.service.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.aliothmoon.maameow.data.datasource.AppDownloader
import com.aliothmoon.maameow.data.model.update.UpdateError
import com.aliothmoon.maameow.data.model.update.UpdateProcessState
import com.aliothmoon.maameow.data.model.update.UpdateSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File

class AppUpdateHandler(
    private val context: Context,
    private val downloader: AppDownloader,
) {
    private val _state = MutableStateFlow<UpdateProcessState>(UpdateProcessState.Idle)
    val state: StateFlow<UpdateProcessState> = _state.asStateFlow()

    /**
     * 检查 App 更新
     */
    suspend fun checkUpdate(source: UpdateSource, cdk: String = "") {
        _state.value = UpdateProcessState.Checking("正在检查应用更新...")

        val result = when (source) {
            UpdateSource.MIRROR_CHYAN -> downloader.checkVersionFromMirrorChyan(cdk)
            UpdateSource.GITHUB -> downloader.checkVersionFromGitHub()
        }

        when (result) {
            is AppDownloader.VersionCheckResult.UpdateAvailable -> {
                _state.value = UpdateProcessState.Available(result.info)
            }

            is AppDownloader.VersionCheckResult.NoUpdate -> {
                _state.value = UpdateProcessState.NoUpdate(result.currentVersion)
            }

            is AppDownloader.VersionCheckResult.Error -> {
                _state.value = UpdateProcessState.Failed(
                    UpdateError.fromCode(result.code, result.message)
                )
            }
        }
    }

    /**
     * 下载并安装 APK
     */
    suspend fun downloadAndInstall(url: String): Result<Unit> {
        _state.value = UpdateProcessState.Downloading(0, "准备下载...", 0L, 0L)

        val downloadResult = downloader.downloadToTempFile(url) { progress ->
            _state.value = UpdateProcessState.Downloading(
                progress = progress.progress,
                speed = progress.speed,
                downloaded = progress.downloaded,
                total = progress.total
            )
        }

        val apkFile = downloadResult.getOrElse { e ->
            _state.value =
                UpdateProcessState.Failed(UpdateError.NetworkError(e.message ?: "下载失败"))
            return Result.failure(e)
        }

        // 触发系统安装
        _state.value = UpdateProcessState.Installing
        return try {
            installApk(apkFile)
            _state.value = UpdateProcessState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "安装 APK 失败")
            _state.value = UpdateProcessState.Failed(UpdateError.UnknownError("安装失败: ${e.message}"))
            Result.failure(e)
        }
    }

    private fun installApk(apkFile: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun resetState() {
        _state.value = UpdateProcessState.Idle
    }
}
