package com.example.opengl360.poc.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.opengl360.poc.subtitles.Subtitle

/**
 * Composable représentant une barre de progression vidéo avec des indicateurs de sous-titres.
 *
 * @param progress La position actuelle de la vidéo (en millisecondes).
 * @param duration La durée totale de la vidéo (en millisecondes).
 * @param subtitles La liste des sous-titres, contenant leurs plages de temps.
 * @param onSeek Fonction appelée lorsqu'un utilisateur clique ou glisse sur la barre.
 */
@Composable
fun CustomVideoSlider(
    sizeFraction : Float,
    progress: Int,
    duration: Int,
    subtitles: List<Subtitle>,
    onSeek: (Int) -> Unit
) {
    val barHeight = 11.dp // Hauteur de la barre
    val circleRadius = 10.dp // Rayon du cercle (plus grand que la barre)
    val boxSize = remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxWidth(sizeFraction)
            .height(circleRadius * 2)
            .padding(horizontal = 16.dp)
            .onGloballyPositioned { coordinates ->
                boxSize.value = coordinates.size
            }
            .pointerInput(boxSize.value, duration) {
                detectTapGestures { offset ->
                    val width = boxSize.value.width.toFloat()
                    if (width > 0) {
                        val newProgress = ((offset.x / width) * duration).toInt()
                        onSeek(newProgress)
                    }
                }
            }
    ) {
        // Dessin du fond de la barre
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = Color(0xFF167459),
                topLeft = Offset(0f, size.height / 2 - barHeight.toPx() / 2),
                size = Size(width = size.width, height = barHeight.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }

        // Dessin des zones de sous-titres par-dessus le fond
        Canvas(modifier = Modifier.matchParentSize()) {
            val subtitleBarHeight = barHeight.toPx() * 0.5f
            val topY = size.height / 2 - subtitleBarHeight / 2

            val inactiveSubtitleColor = Color(0x60FFFFFF)
            val activeSubtitleColor = Color(0xFFB2FF59) // Vert clair

            subtitles.forEach { subtitle ->
                val start = subtitle.startTime
                val end = subtitle.endTime
                val startX = (start.toFloat() / duration) * size.width
                val endX = (end.toFloat() / duration) * size.width
                val isActive = progress >= start && progress <= end

                drawRoundRect(
                    color = if (isActive) activeSubtitleColor else inactiveSubtitleColor,
                    topLeft = Offset(x = startX, y = topY),
                    size = Size(width = endX - startX, height = subtitleBarHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }

        // Dessin de la progression actuelle de la vidéo
        Canvas(modifier = Modifier.matchParentSize()) {
            val progressWidth = (progress.toFloat() / duration) * size.width
            drawRoundRect(
                color = Color(0xFF23CF76),
                topLeft = Offset(0f, size.height / 2 - barHeight.toPx() / 2),
                size = Size(width = progressWidth, height = barHeight.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }

        // Dessin du cercle indiquant la progression
        Canvas(modifier = Modifier.matchParentSize()) {
            val circleX = (progress.toFloat() / duration) * size.width
            val circleY = size.height / 2 // Centré verticalement
            drawCircle(
                color = Color.White,
                radius = circleRadius.toPx(), // Taille légèrement plus grande que la barre
                center = Offset(x = circleX, y = circleY)
            )
        }
    }
}

