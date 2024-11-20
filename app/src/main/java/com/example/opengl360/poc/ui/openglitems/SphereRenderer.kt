package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import com.example.opengl360.poc.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Classe responsable du rendu OpenGL pour afficher une sphère avec une texture vidéo.
 *
 * @param context Contexte de l'application
 */
class SphereRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var sphere: Sphere
    private lateinit var surfaceTexture: SurfaceTexture
    private var mediaPlayer: MediaPlayer? = null
    private var textureId: Int = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private var rotationX = 0f
    private var rotationY = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Définit la couleur de fond (blanc)
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Initialise la sphère pour le rendu
        sphere = Sphere(40, 40, 1f)

        // Génère une texture externe pour la vidéo
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        surfaceTexture = SurfaceTexture(textureId)

        // Initialise le lecteur multimédia
        mediaPlayer = MediaPlayer.create(context, R.raw.bundle)
        mediaPlayer?.setSurface(Surface(surfaceTexture))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, aspectRatio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()

        // Configure la matrice de vue
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f)
        Matrix.rotateM(viewMatrix, 0, rotationX, 0f, 1f, 0f)
        Matrix.rotateM(viewMatrix, 0, rotationY, 1f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Rendu de la sphère avec la texture vidéo
        sphere.drawWithVideoTexture(modelViewProjectionMatrix, textureId)
    }

    /**
     * Met à jour les rotations de la caméra en fonction des mouvements tactiles.
     *
     * @param deltaX Déplacement horizontal
     * @param deltaY Déplacement vertical
     */
    fun updateRotation(deltaX: Float, deltaY: Float) {
        rotationX += deltaX * 0.1f
        rotationY = (rotationY + deltaY * 0.1f).coerceIn(-85f, 85f) // Limite le pitch
    }

    /**
     * Définit l'état de lecture de la vidéo.
     *
     * @param isPlaying Indique si la vidéo doit être en lecture (true) ou en pause (false)
     */
    fun setPlayingState(isPlaying: Boolean) {
        mediaPlayer?.let {
            if (isPlaying) {
                it.start()
            } else {
                it.pause()
            }
        }
    }

    /**
     * Récupère la progression et la durée de la vidéo.
     *
     * @return Une paire contenant la position actuelle et la durée totale (en millisecondes)
     */
    fun getVideoProgress(): Pair<Int, Int> {
        return mediaPlayer?.let {
            Pair(it.currentPosition, it.duration)
        } ?: Pair(0, 1)
    }

    /**
     * Définit la position de lecture de la vidéo.
     *
     * @param position Position en millisecondes
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    /**
     * Récupère l'orientation actuelle de la caméra.
     *
     * @return Une paire avec les rotations horizontale (Yaw) et verticale (Pitch)
     */
    fun getCameraOrientation(): Pair<Float, Float> {
        return Pair(rotationX, rotationY)
    }
}
