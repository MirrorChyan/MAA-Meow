package com.aliothmoon.maameow.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun ShizukuPermissionDialog(
    isRequesting: Boolean,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(
                text = "需要 Shizuku 权限",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = "后台任务页面依赖 Shizuku 权限，授权成功前将持续显示该提示。",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isRequesting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(if (isRequesting) "授权中..." else "立即授权")
            }
        },
    )
}

