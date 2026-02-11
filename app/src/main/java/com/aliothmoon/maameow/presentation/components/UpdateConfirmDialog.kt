package com.aliothmoon.maameow.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.aliothmoon.maameow.data.model.update.UpdateInfo

/**
 * 更新确认弹窗
 * @param updateInfo 更新信息
 * @param onConfirm 确认下载回调
 * @param onDismiss 取消回调
 */
@Composable
fun UpdateConfirmDialog(
    updateInfo: UpdateInfo,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "发现新版本资源",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = buildString {
                    append("检测到新版本资源，是否立即下载？")
                    if (updateInfo.version.isNotBlank()) {
                        append("\n\n版本: ${updateInfo.version}")
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("立即下载")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("稍后再说")
            }
        }
    )
}
