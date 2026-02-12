package com.aliothmoon.maameow.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import com.aliothmoon.maameow.manager.ShizukuInstallHelper

@Composable
fun ShizukuPermissionDialog(
    isRequesting: Boolean,
    onConfirm: () -> Unit,
) {
    val context = LocalContext.current
    val isInstalled = ShizukuInstallHelper.isShizukuInstalled(context)

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(
                text = if (isInstalled) "需要 Shizuku 权限" else "未检测到 Shizuku",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = if (isInstalled) {
                    "后台任务页面依赖 Shizuku 权限，授权成功前将持续显示该提示。"
                } else {
                    "后台任务依赖 Shizuku 服务，检测到设备未安装 Shizuku，点击下方按钮安装。"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            if (isInstalled) {
                Button(
                    onClick = onConfirm,
                    enabled = !isRequesting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(if (isRequesting) "授权中..." else "立即授权")
                }
            } else {
                Button(
                    onClick = { ShizukuInstallHelper.installShizuku(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("安装 Shizuku")
                }
            }
        },
    )
}
