package com.aliothmoon.maameow.presentation.view.panel

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.aliothmoon.maameow.data.model.ReclamationConfig


@Composable
fun ReclamationConfigPanel(config: ReclamationConfig, onConfigChange: (ReclamationConfig) -> Unit) {
    Text("施工中")
}

