package com.aliothmoon.maameow.domain.service.update

import com.aliothmoon.maameow.data.model.update.UpdateProcessState
import com.aliothmoon.maameow.data.model.update.UpdateSource
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * 更新服务统一门面
 * 提供资源更新和 App 更新的统一入口
 * 保持 API 兼容，内部委托给具体 Handler
 */
class UpdateService(
    private val resourceHandler: ResourceUpdateHandler,
    private val appHandler: AppUpdateHandler,
) {
    // ==================== 资源更新 ====================

    val resourceUpdateState: StateFlow<UpdateProcessState>
        get() = resourceHandler.state

    suspend fun checkFromMirrorChyan(current: String, cdk: String = "") {
        resourceHandler.checkUpdate(current, cdk)
    }

    suspend fun downloadResource(dir: File, url: String): Result<Unit> {
        return resourceHandler.downloadAndInstall(dir, url)
    }

    fun reset() {
        resourceHandler.resetState()
    }

    // ==================== App 更新 ====================

    val appUpdateState: StateFlow<UpdateProcessState>
        get() = appHandler.state

    suspend fun checkAppUpdate(source: UpdateSource, cdk: String = "") {
        appHandler.checkUpdate(source, cdk)
    }

    suspend fun downloadAndInstallApp(url: String): Result<Unit> {
        return appHandler.downloadAndInstall(url)
    }

    fun resetAppUpdate() {
        appHandler.resetState()
    }
}
