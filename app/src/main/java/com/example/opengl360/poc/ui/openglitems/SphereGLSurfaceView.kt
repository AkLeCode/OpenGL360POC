package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

/**
 * Cette classe configure une vue OpenGL pour afficher une sphère 360°.
 * Elle gère les interactions tactiles pour faire pivoter la caméra autour de la sphère.
 *
 * @param context Contexte de l'application
 */
class SphereGLSurfaceView(context: Context) : GLSurfaceView(context) {

    // Instance du renderer personnalisé
    val renderer: SphereRenderer
    var isInit: Boolean = false

    init {
        // Configuration d'OpenGL ES 3.0
        setEGLContextClientVersion(3)
        renderer = SphereRenderer(context)
        setRenderer(renderer)

        // Définit le mode de rendu continu
        renderMode = RENDERMODE_CONTINUOUSLY

        // Configure le format du rendu pour des surfaces transparentes
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Variables pour suivre les interactions tactiles
        var previousX = 0f
        var previousY = 0f

        // Listener pour les interactions tactiles
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - previousX
                    val dy = event.y - previousY

                    // Met à jour la rotation de la caméra en fonction des mouvements tactiles
                    renderer.updateRotation(dx, dy)
                    requestRender() // Demande un nouveau rendu après interaction
                }
            }
            // Met à jour les coordonnées précédentes pour le prochain événement tactile
            previousX = event.x
            previousY = event.y
            true
        }

        isInit = true
    }

    /**
     * Récupère la progression actuelle et la durée totale de la vidéo.
     *
     * @return Une paire contenant la progression actuelle (en millisecondes)
     * et la durée totale (en millisecondes).
     */
    fun getVideoProgress(): Pair<Int, Int> {
        return renderer.getVideoProgress()
    }

    /**
     * Met à jour l'état de lecture de la vidéo (lecture ou pause).
     *
     * @param isPlaying Indique si la vidéo doit être en lecture (true) ou en pause (false).
     */
    fun updatePlayingState(isPlaying: Boolean) {
        renderer.setPlayingState(isPlaying)
    }

    /**
     * Positionne la vidéo à un moment donné.
     *
     * @param position Position en millisecondes où positionner la vidéo.
     */
    fun seekTo(position: Int) {
        renderer.seekTo(position)
    }

    /**
     * Récupère l'orientation actuelle de la caméra.
     *
     * @return Une paire contenant la rotation horizontale (Yaw) et la rotation verticale (Pitch).
     */
    fun getCameraOrientation(): Pair<Float, Float> {
        return renderer.getCameraOrientation()
    }
}
