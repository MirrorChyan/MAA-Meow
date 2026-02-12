package com.aliothmoon.maameow.presentation.view.panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliothmoon.maameow.data.model.TaskItem
import com.aliothmoon.maameow.data.model.TaskType
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * 左侧任务列表（支持拖拽排序）
 */
@Composable
fun TaskListPanel(
    tasks: List<TaskItem>,
    selectedTaskType: TaskType?,
    onTaskEnabledChange: (TaskType, Boolean) -> Unit,
    onTaskSelected: (TaskType) -> Unit,
    onTaskMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val state = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        onMove = { from, to ->
            onTaskMove(from.index, to.index)
        }
    )

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(tasks, key = { _, item -> item.type }) { index, task ->
            ReorderableItem(state, key = task.type) { isDragging ->
                TaskItemRow(
                    task = task,
                    isSelected = selectedTaskType?.id == task.type,
                    onEnabledChange = { enabled ->
                        task.toTaskType()?.let { taskType ->
                            onTaskEnabledChange(taskType, enabled)
                        }
                    },
                    onSelected = {
                        task.toTaskType()?.let { taskType ->
                            onTaskSelected(taskType)
                        }
                    },
                    modifier = Modifier.longPressDraggableHandle()
                )
            }
        }
    }
}

/**
 * 任务项行
 */
@Composable
private fun TaskItemRow(
    task: TaskItem,
    isSelected: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val taskType = task.toTaskType()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xfff2f3f5)
            } else Color.White
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelected() }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isEnabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = taskType?.displayName ?: task.type,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) Color(0xFF2196F3) else Color.Gray
            )
        }
    }
}
