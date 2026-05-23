package com.example.ui.screens

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter

data class StrokeData(
    val color: Color = Color.Red,
    val width: Float = 10f,
    val path: Path = Path()
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DoodleOverlay() {
    val strokes = remember { mutableStateListOf<StrokeData>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentColor by remember { mutableStateOf(Color.Red) } // Simple implementation for presentation

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInteropFilter { event ->
                val x = event.x
                val y = event.y

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        currentPath = Path().apply {
                            moveTo(x, y)
                        }
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentPath?.lineTo(x, y)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        currentPath?.let {
                            strokes.add(StrokeData(color = currentColor, width = 10f, path = it))
                        }
                        currentPath = null
                        true
                    }
                    else -> false
                }
            }
    ) {
        for (stroke in strokes) {
            drawPath(
                path = stroke.path,
                color = stroke.color,
                style = Stroke(width = stroke.width)
            )
        }
        currentPath?.let {
            drawPath(
                path = it,
                color = currentColor,
                style = Stroke(width = 10f)
            )
        }
    }
}
