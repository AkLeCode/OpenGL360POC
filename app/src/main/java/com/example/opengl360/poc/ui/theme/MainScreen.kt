package com.example.opengl360.poc.ui.theme

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.opengl360.poc.R
import com.example.opengl360.poc.ui.openglitems.SphereGLSurfaceView
import kotlinx.coroutines.delay

/**
 * Composable principal représentant l'écran de visualisation de la vidéo 360°.
 * Il inclut la sphère 360°, les contrôles de lecture et un indicateur de rotation.
 */
@Composable
fun MainScreen() {
    // État de lecture de la vidéo
    var isPlaying by remember { mutableStateOf(true) }
    // Progression actuelle de la vidéo
    var videoProgress by remember { mutableStateOf(0) }
    // Durée totale de la vidéo
    var videoDuration by remember { mutableStateOf(1) }
    // Indique si l'affichage est en plein écran
    var isFullscreen by remember { mutableStateOf(false) }
    // Rotation horizontale de la caméra
    var rotationY by remember { mutableStateOf(0f) }

    // Référence mutable à l'instance de SphereGLSurfaceView
    val sphereGLSurfaceViewRef = remember { mutableStateOf<SphereGLSurfaceView?>(null) }

    // Mise à jour périodique des informations vidéo (progression et durée)
    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // Mise à jour toutes les 100ms
            val sphereGLSurfaceView = sphereGLSurfaceViewRef.value
            if (sphereGLSurfaceView != null && sphereGLSurfaceView.isInit) {
                val (progress, duration) = sphereGLSurfaceView.getVideoProgress()
                videoProgress = progress
                videoDuration = duration

                // Mise à jour de la rotation horizontale
                val (y) = sphereGLSurfaceView.getCameraOrientation()
                rotationY = y
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Zone contenant la vue de la sphère 360° et les contrôles
        Box(
            modifier =
            if (isFullscreen)
                Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            else
                Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.Center)
                    .clip(shape = ShapeDefaults.Small)
        ) {

            // Vue Android pour l'intégration d'OpenGL
            AndroidView(
                factory = { context ->
                    SphereGLSurfaceView(context).apply {
                        updatePlayingState(isPlaying)
                        sphereGLSurfaceViewRef.value = this // Lien avec la référence mutable
                    }
                },
                update = { view ->
                    view.updatePlayingState(isPlaying)
                },
                modifier = Modifier.fillMaxSize().background(Color.Transparent)
            )

            // Indicateur de rotation 2D
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                RotationIndicator2D(rotationY = rotationY)
            }

            // Barre de contrôle de la vidéo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color(0x80000000)) // Fond semi-transparent
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Bouton Lecture/Pause
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) ImageVector.vectorResource(R.drawable.pause) else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(4.dp))

                    // Barre de progression de la vidéo
                    Slider(
                        value = videoProgress.toFloat(),
                        valueRange = 0f..videoDuration.toFloat(),
                        onValueChange = { newValue -> videoProgress = newValue.toInt() },
                        onValueChangeFinished = {
                            // Modification de la position de lecture dans la vidéo
                            sphereGLSurfaceViewRef.value?.seekTo(videoProgress)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF00FF00),
                            inactiveTrackColor = Color.Gray
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Texte affichant la progression et la durée
                    Text(
                        text = "${formatTime(videoProgress)} / ${formatTime(videoDuration)}",
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Spacer(Modifier.width(4.dp))

                    // Bouton Plein écran
                    IconButton(
                        onClick = { isFullscreen = !isFullscreen },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullscreen) ImageVector.vectorResource(R.drawable.fullon) else ImageVector.vectorResource(R.drawable.fulloff),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Formate une durée exprimée en millisecondes au format mm:ss.
 *
 * @param milliseconds Durée en millisecondes.
 * @return Chaîne formatée au format mm:ss.
 */
private fun formatTime(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Composable affichant un indicateur de rotation 2D.
 * Cet indicateur affiche une flèche indiquant la direction horizontale.
 *
 * @param rotationY Rotation horizontale en degrés.
 */
@Composable
fun RotationIndicator2D(rotationY: Float) {
    val offsetAngle = -90f // Décalage pour centrer la flèche vers le haut

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(Color.Transparent, shape = CircleShape)
            .padding(8.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f

            // Dessine un cercle extérieur
            drawCircle(
                color = Color(0x80000000),
                radius = radius,
                center = this.center
            )

            // Dessine une flèche indiquant la rotation horizontale
            val angle = (rotationY - offsetAngle).toRadians()
            val indicatorX = this.center.x + (radius) * kotlin.math.cos(angle)
            val indicatorY = this.center.y + (radius) * kotlin.math.sin(angle)

            drawLine(
                color = Color.White,
                start = this.center,
                end = Offset(indicatorX.toFloat(), indicatorY.toFloat()),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
}

/**
 * Extension pour convertir un angle en degrés en radians.
 *
 * @return L'angle en radians.
 */
fun Float.toRadians() = (this / 180f * kotlin.math.PI).toFloat()
