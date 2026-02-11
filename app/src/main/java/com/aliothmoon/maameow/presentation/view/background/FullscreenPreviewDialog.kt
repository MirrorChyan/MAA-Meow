package com.aliothmoon.maameow.presentation.view.background

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aliothmoon.maameow.constant.DefaultDisplayConfig

@Composable
fun FullscreenPreviewDialog(
    onSurfaceAvailable: (Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var previewSurface by remember { mutableStateOf<Surface?>(null) }

    DisposableEffect(Unit) {
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

        onDispose {
            if (originalOrientation != null) {
                activity.requestedOrientation = originalOrientation
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { contextForTexture ->
                    TextureView(contextForTexture).apply {
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
                                onSurfaceAvailable(surface)
                                updateFullscreenTransform(this@apply, width, height)
                            }

                            override fun onSurfaceTextureSizeChanged(
                                surfaceTexture: SurfaceTexture,
                                width: Int,
                                height: Int
                            ) {
                                updateFullscreenTransform(this@apply, width, height)
                            }

                            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                                previewSurface?.release()
                                previewSurface = null
                                onSurfaceDestroyed()
                                return true
                            }

                            override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) = Unit
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = {})
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private fun updateFullscreenTransform(textureView: TextureView, viewWidth: Int, viewHeight: Int) {
    if (viewWidth == 0 || viewHeight == 0) return

    val viewWidthFloat = viewWidth.toFloat()
    val viewHeightFloat = viewHeight.toFloat()
    val bufferWidth = DefaultDisplayConfig.WIDTH.toFloat()
    val bufferHeight = DefaultDisplayConfig.HEIGHT.toFloat()

    val matrix = Matrix()
    val scale = minOf(viewWidthFloat / bufferWidth, viewHeightFloat / bufferHeight)

    matrix.postScale(bufferWidth / viewWidthFloat, bufferHeight / viewHeightFloat)
    matrix.postScale(scale, scale)

    val scaledWidth = bufferWidth * scale
    val scaledHeight = bufferHeight * scale
    val offsetX = (viewWidthFloat - scaledWidth) / 2f
    val offsetY = (viewHeightFloat - scaledHeight) / 2f
    matrix.postTranslate(offsetX, offsetY)

    textureView.setTransform(matrix)
}

