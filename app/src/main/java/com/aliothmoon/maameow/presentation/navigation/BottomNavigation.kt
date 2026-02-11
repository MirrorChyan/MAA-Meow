package com.aliothmoon.maameow.presentation.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aliothmoon.maameow.constant.Routes

sealed class BottomNavTab(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object HOME : BottomNavTab(
        route = Routes.HOME,
        label = "首页",
        icon = Icons.Default.Home
    )

    data object BACKGROUND : BottomNavTab(
        route = Routes.BACKGROUND_TASK,
        label = "后台任务",
        icon = Icons.Default.PlayArrow
    )

    companion object {
        val all = listOf(HOME, BACKGROUND)
    }
}

@Composable
fun AppBottomNavigation(
    currentRoute: String,
    onTabSelected: (BottomNavTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.height(56.dp)
    ) {
        BottomNavTab.all.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = {
                    Text(text = tab.label)
                }
            )
        }
    }
}
