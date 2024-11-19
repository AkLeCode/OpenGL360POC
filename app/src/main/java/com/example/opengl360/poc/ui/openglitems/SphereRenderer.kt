package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.Surface
import com.example.opengl360.poc.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.fixedRateTimer
import kotlin.math.cos
import kotlin.math.sin

class SphereRenderer(private val context: Context) : GLSurfaceView.Renderer {
    // Déclare les objets nécessaires au rendu
    private lateinit var sphere: Sphere
    private lateinit var surfaceTexture: SurfaceTexture
    private var mediaPlayer: MediaPlayer? = null
    private var textureId: Int = 0

    // Matrices pour la projection et la vue
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    // Variables pour contrôler la rotation
    private var rotationYaw = 0f // Yaw (rotation horizontale)
    private var rotationPitch = 0f // Pitch (rotation verticale)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Configure le fond blanc et active le test de profondeur
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        // Crée la sphère
        sphere = Sphere(100, 100, 1f)

        // Initialise la texture vidéo
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        surfaceTexture = SurfaceTexture(textureId)

        // Configure le MediaPlayer
        mediaPlayer = MediaPlayer.create(context, R.raw.bundle)
        mediaPlayer?.setSurface(Surface(surfaceTexture))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        val fovY = 35f // FOV réduit pour atténuer l'effet "fish-eye"
        Matrix.perspectiveM(projectionMatrix, 0, fovY, aspectRatio, 0.1f, 100f) // Passez de 90f à 60f
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()

        // Distance constante de la caméra par rapport au centre
        val radius = 1f

        // Calcul de la position de la caméra en fonction du Yaw et Pitch
        val eyeX = radius * cos(Math.toRadians(rotationPitch.toDouble())) * sin(Math.toRadians(rotationYaw.toDouble())).toFloat()
        val eyeY = radius * sin(Math.toRadians(rotationPitch.toDouble())).toFloat()
        val eyeZ = radius * cos(Math.toRadians(rotationPitch.toDouble())) * cos(Math.toRadians(rotationYaw.toDouble())).toFloat()

        // Point que la caméra regarde (centre de la sphère)
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // Vecteur "up" pour maintenir la caméra droite
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        // Mise à jour de la matrice de vue
        Matrix.setLookAtM(viewMatrix, 0, eyeX.toFloat(), eyeY, eyeZ.toFloat(), centerX, centerY, centerZ, upX, upY, upZ)

        // Combinaison des matrices de projection et de vue
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Rendu de la sphère avec la texture vidéo
        sphere.drawWithVideoTexture(modelViewProjectionMatrix, textureId)
    }

    fun updateRotation(deltaYaw: Float, deltaPitch: Float) {
        val sensitivity = 0.1f // Sensibilité des mouvements
        rotationYaw = (rotationYaw + deltaYaw * sensitivity) % 360f
        rotationPitch = (rotationPitch - deltaPitch * sensitivity).coerceIn(-75f, 75f) // Limitation plus stricte
    }

    fun setPlayingState(isPlaying: Boolean) {
        // Met en pause ou démarre la vidéo
        mediaPlayer?.let {
            if (isPlaying) {
                it.start()
            } else {
                it.pause()
            }
        }
    }

    fun getVideoProgress(): Pair<Int, Int> {
        // Retourne la position actuelle et la durée totale de la vidéo
        return mediaPlayer?.let {
            Pair(it.currentPosition, it.duration)
        } ?: Pair(0, 1)
    }

    fun seekTo(position: Int) {
        // Permet de chercher une position précise dans la vidéo
        mediaPlayer?.seekTo(position)
    }

    fun getCameraOrientation(): Pair<Float, Float> {
        // Retourne les angles de rotation (Yaw et Pitch)
        return Pair(rotationYaw, rotationPitch)
    }
}
