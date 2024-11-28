package com.example.opengl360.poc

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.opengl360.poc.subtitles.SubtitleManager
import com.example.opengl360.poc.ui.screen.MainScreen
import com.example.opengl360.poc.ui.theme.OpenGL360POCTheme

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val moviesPath = moviesDir.absolutePath
        val srtPath = "$moviesPath/beauval-1/srt/video.srt"

        val subtitleManager = SubtitleManager(this, srtPath)

        // Initialisez le lanceur de demande de permission
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // La permission est accordée. Vous pouvez accéder au fichier
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
            } else {
                // La permission est refusée. Gérez ce cas (afficher un message à l'utilisateur, désactiver des fonctionnalités, etc.)
            }
        }

        // Vérifiez si la permission est déjà accordée
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            // La permission est déjà accordée
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
        } else {
            // Demandez la permission
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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
