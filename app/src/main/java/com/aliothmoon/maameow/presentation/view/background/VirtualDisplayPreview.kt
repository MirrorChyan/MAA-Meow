package com.aliothmoon.maameow.presentation.view.background

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aliothmoon.maameow.constant.DefaultDisplayConfig

@Composable
fun VirtualDisplayPreview(
    isRunning: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onSurfaceAvailable: (Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSurfaceAvailable by remember { mutableStateOf(false) }
    var previewSurface by remember { mutableStateOf<Surface?>(null) }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight

        val aspectRatio = 16f / 9f
        val widthFromHeight = maxHeight * aspectRatio
        val heightFromWidth = maxWidth / aspectRatio

        val (cardWidth, cardHeight) = if (widthFromHeight <= maxWidth) {
            widthFromHeight to maxHeight
        } else {
            maxWidth to heightFromWidth
        }

        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .clickable(onClick = onClick),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        TextureView(context).apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(
                                    surfaceTexture: SurfaceTexture,
                                    width: Int,
                                    height: Int
                                ) {
                                    surfaceTexture.setDefaultBufferSize(
                                        DefaultDisplayConfig.WIDTH,
                                        DefaultDisplayConfig.HEIGHT
                                    )
                                    previewSurface?.release()
                                    val surface = Surface(surfaceTexture)
                                    previewSurface = surface
                                    isSurfaceAvailable = true
                                    onSurfaceAvailable(surface)
                                    updateTextureTransform(this@apply, width, height)
                                }

                                override fun onSurfaceTextureSizeChanged(
                                    surfaceTexture: SurfaceTexture,
                                    width: Int,
                                    height: Int
                                ) {
                                    updateTextureTransform(this@apply, width, height)
                                }

                                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                    isSurfaceAvailable = false
                                    previewSurface?.release()
                                    previewSurface = null
                                    onSurfaceDestroyed()
                                    return true
                                }

                                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "加载中...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = onRetry) {
                                    Text(text = "重试")
                                }
                            }
                        }
                    }

                    !isRunning -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "待执行",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    !isSurfaceAvailable -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "等待画面中...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun updateTextureTransform(textureView: TextureView, viewWidth: Int, viewHeight: Int) {
    if (viewWidth == 0 || viewHeight == 0) return

    val viewW = viewWidth.toFloat()
    val viewH = viewHeight.toFloat()
    val bufferW = DefaultDisplayConfig.WIDTH.toFloat()
    val bufferH = DefaultDisplayConfig.HEIGHT.toFloat()

    val matrix = Matrix()
    val scale = minOf(viewW / bufferW, viewH / bufferH)

    matrix.postScale(bufferW / viewW, bufferH / viewH)
    matrix.postScale(scale, scale)

    val scaledW = bufferW * scale
    val scaledH = bufferH * scale
    val offsetX = (viewW - scaledW) / 2f
    val offsetY = (viewH - scaledH) / 2f
    matrix.postTranslate(offsetX, offsetY)

    textureView.setTransform(matrix)
}
