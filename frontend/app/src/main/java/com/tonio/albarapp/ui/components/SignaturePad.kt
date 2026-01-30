package com.tonio.albarapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Simple signature pad that tracks strokes in-memory.
 * Use [SignaturePadState.hasSignature] to know if the user drew something,
 * and call [SignaturePadState.clear()] to reset.
 */
class SignaturePadState {
    val paths = mutableStateListOf<Path>()
    var hasSignature by mutableStateOf(false)
        private set

    internal fun addPath(p: Path) {
        paths.add(p)
        hasSignature = paths.isNotEmpty()
    }

    fun clear() {
        paths.clear()
        hasSignature = false
    }
}

@Composable
fun rememberSignaturePadState() = remember { SignaturePadState() }

@Composable
fun SignaturePad(
    label: String,
    state: SignaturePadState,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, top = 12.dp)
        )

        Box(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
                .height(160.dp)
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val path = Path().apply { moveTo(offset.x, offset.y) }
                            state.addPath(path)
                        },
                        onDrag = { change, _ ->
                            if (state.paths.isNotEmpty()) {
                                state.paths.last().lineTo(change.position.x, change.position.y)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                state.paths.forEach { p ->
                    drawPath(p, color = Color.Black, style = Stroke(width = 3f))
                }
            }
        }

        TextButton(
            onClick = { state.clear() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) { Text("Clear") }
    }
}
