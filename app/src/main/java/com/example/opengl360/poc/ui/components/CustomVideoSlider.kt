package com.example.opengl360.poc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
    val barHeight = 12.dp // Hauteur de la barre
    val circleRadius = 13.dp // Rayon du cercle (plus grand que la barre)

    Box(
        modifier = Modifier
            .fillMaxWidth(sizeFraction) // Limite à 90% pour laisser de l'espace
            .height(circleRadius * 2) // Hauteur suffisante pour inclure le cercle
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = ((offset.x / size.width) * duration).toInt()
                    onSeek(newProgress)
                }
            }
    ) {
        // Dessin du fond de la barre
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                color = Color.LightGray,
                topLeft = Offset(0f, size.height / 2 - barHeight.toPx() / 2), // Centré verticalement
                size = Size(width = size.width, height = barHeight.toPx())
            )
        }

        // Dessin des zones de sous-titres par-dessus le fond
        Canvas(modifier = Modifier.matchParentSize()) {
            val subtitleRanges = subtitles.map { it.startTime to it.endTime }
            subtitleRanges.forEach { (start, end) ->
                val startX = (start.toFloat() / duration) * size.width
                val endX = (end.toFloat() / duration) * size.width
                drawRect(
                    color = Color.Gray.copy(alpha = 0.4f),
                    topLeft = Offset(x = startX, y = size.height / 2 - barHeight.toPx() / 2),
                    size = Size(width = endX - startX, height = barHeight.toPx())
                )
            }
        }

        // Dessin de la progression actuelle de la vidéo
        Canvas(modifier = Modifier.matchParentSize()) {
            val progressWidth = (progress.toFloat() / duration) * size.width
            drawRect(
                color = Color(0xFF23CF76),
                topLeft = Offset(0f, size.height / 2 - barHeight.toPx() / 2),
                size = Size(width = progressWidth, height = barHeight.toPx())
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
