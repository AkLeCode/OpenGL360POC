package com.example.opengl360.poc

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.opengl360.poc.subtitles.SubtitleManager
import com.example.opengl360.poc.ui.screen.MainScreen
import com.example.opengl360.poc.ui.theme.OpenGL360POCTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val subtitleManager = SubtitleManager(this, R.raw.beauval_1_srt_video)
        setContent {
            OpenGL360POCTheme {
                Surface(
                    Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {

                    MainScreen(subtitleManager)
                }
            }
        }
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN       // Masque la barre de statut
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // Masque les boutons de navigation
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Active le mode immersif
                )
        actionBar?.hide()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OpenGL360POCTheme {
        Greeting("Android")
    }
}