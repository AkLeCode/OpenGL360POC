package com.example.opengl360.poc.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.opengl360.poc.R
import com.example.opengl360.poc.ui.openglitems.SphereGLSurfaceView
import kotlinx.coroutines.delay

/**
 * Écran principal affichant une vidéo 360° interactive.
 * Inclut des contrôles pour lire/pauser la vidéo, régler la progression et activer le mode plein écran.
 */
@Composable
fun MainScreen() {
    var isPlaying by remember { mutableStateOf(true) }
    var videoProgress by remember { mutableStateOf(0) }
    var videoDuration by remember { mutableStateOf(1) }
    val sphereGLSurfaceViewRef = remember { mutableStateOf<SphereGLSurfaceView?>(null) }

    // Mise à jour régulière de la progression vidéo
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            val sphereGLSurfaceView = sphereGLSurfaceViewRef.value
            if (sphereGLSurfaceView != null && sphereGLSurfaceView.isInit) {
                val (progress, duration) = sphereGLSurfaceView.getVideoProgress()
                videoProgress = progress
                videoDuration = duration
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // AndroidView pour afficher la sphère 360°
        AndroidView(
            factory = { context ->
                SphereGLSurfaceView(context).apply {
                    updatePlayingState(isPlaying)
                    sphereGLSurfaceViewRef.value = this
                }
            },
            update = { view ->
                view.updatePlayingState(isPlaying)
            },
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
        )

        // Contrôles vidéo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color(0x80000000))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { isPlaying = !isPlaying }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Slider(
                    value = videoProgress.toFloat(),
                    valueRange = 0f..videoDuration.toFloat(),
                    onValueChange = { newValue ->
                        videoProgress = newValue.toInt()
                        sphereGLSurfaceViewRef.value?.seekTo(videoProgress)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.Green
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${formatTime(videoProgress)} / ${formatTime(videoDuration)}",
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Formate un temps en millisecondes en une chaîne de type mm:ss.
 *
 * @param milliseconds Temps en millisecondes
 * @return Chaîne formatée
 */
private fun formatTime(milliseconds: Int): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
