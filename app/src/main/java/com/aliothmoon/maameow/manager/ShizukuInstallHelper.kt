package com.aliothmoon.maameow.manager

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File

object ShizukuInstallHelper {

    private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    private const val ASSET_NAME = "shizuku.apk"

    fun isShizukuInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun installShizuku(context: Context): Boolean {
        return try {
            val apkFile = copyApkFromAssets(context) ?: return false
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to install Shizuku")
            false
        }
    }

    private fun copyApkFromAssets(context: Context): File? {
        return try {
            val destFile = File(context.cacheDir, ASSET_NAME)
            context.assets.open(ASSET_NAME).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile
        } catch (e: Exception) {
            Timber.e(e, "Failed to copy Shizuku APK from assets")
            null
        }
    }
}
