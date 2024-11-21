package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

/**
 * SphereGLSurfaceView est une vue OpenGL configurée pour afficher une sphère texturée
 * avec un rendu interactif et supportant les interactions tactiles.
 *
 * @param context Contexte de l'application.
 */
class SphereGLSurfaceView(context: Context) : GLSurfaceView(context) {
    val renderer: SphereRenderer // Rendu personnalisé pour gérer la sphère et la vidéo
    var isInit: Boolean = false // Indique si la vue a été initialisée

    init {
        // Configure la précision des couleurs et le support de la transparence
        setEGLConfigChooser(8, 8, 8, 8, 16, 4)

        // Configure OpenGL ES 3.0
        setEGLContextClientVersion(3)

        // Initialise le renderer
        renderer = SphereRenderer(context)
        setRenderer(renderer)

        // Définit le mode de rendu en continu
        renderMode = RENDERMODE_CONTINUOUSLY

        // Configure la transparence de l'arrière-plan
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Variables pour stocker les positions tactiles précédentes
        var previousX = 0f
        var previousY = 0f

        // Gestion des interactions tactiles pour faire pivoter la caméra
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    // Calcule le décalage horizontal et vertical
                    val dx = event.x - previousX
                    val dy = event.y - previousY

                    // Met à jour la rotation dans le renderer
                    renderer.updateRotation(dx, dy)
                    requestRender() // Redessine la scène après interaction
                }
            }
            previousX = event.x
            previousY = event.y
            true
        }

        // Marque la vue comme initialisée
        isInit = true
    }

    /**
     * Récupère la progression actuelle et la durée totale de la vidéo.
     *
     * @return Une paire contenant la progression actuelle (ms) et la durée totale (ms).
     */
    fun getVideoProgress(): Pair<Int, Int> {
        return renderer.getVideoProgress()
    }

    /**
     * Met à jour l'état de lecture de la vidéo.
     *
     * @param isPlaying Si vrai, la vidéo est en lecture ; sinon, elle est en pause.
     */
    fun updatePlayingState(isPlaying: Boolean) {
        renderer.setPlayingState(isPlaying)
    }

    /**
     * Positionne la vidéo à un moment précis.
     *
     * @param position Position dans la vidéo en millisecondes.
     */
    fun seekTo(position: Int) {
        renderer.seekTo(position)
    }

    /**
     * Récupère les angles de la caméra (Yaw et Pitch).
     *
     * @return Une paire contenant les angles Yaw (horizontal) et Pitch (vertical).
     */
    fun getCameraOrientation(): Pair<Float, Float> {
        return renderer.getCameraOrientation()
    }
}
