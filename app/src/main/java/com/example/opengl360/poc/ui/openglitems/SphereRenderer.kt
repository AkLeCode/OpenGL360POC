package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import android.view.MotionEvent
import com.example.opengl360.poc.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.concurrent.fixedRateTimer

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
    private var previousX = 0f
    private var previousY = 0f
    private var cameraYaw = 0f
    private var cameraPitch = 0f


    private var progressUpdateListener: ((Int, Int) -> Unit)? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 0f, 0f, 1.0f) // Définit un fond blanc RGBA
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glCullFace(GLES30.GL_FRONT)

        sphere = Sphere(30, 30, 1f)

        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures[0]

        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        surfaceTexture = SurfaceTexture(textureId)

        mediaPlayer = MediaPlayer.create(context, R.raw.bundle)
        mediaPlayer?.setSurface(Surface(surfaceTexture))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        startProgressUpdates()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.1f, 100f)

        // Position de la caméra à l'intérieur de la sphère
        // Caméra au centre, regardant dans la direction positive de Z
        Matrix.setLookAtM(
            viewMatrix, 0,
            0f, 0f, 0f, // Position de la caméra (intérieur de la sphère)
            0f, 0f, -1f, // Point regardé (direction négative de Z)
            0f, 1f, 0f   // Axe "up" (direction Y)
        )
    }

    /*override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, aspectRatio, 0.1f, 100f)
    }*/

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Met à jour le contenu de la texture vidéo
        surfaceTexture.updateTexImage()

        // Applique les rotations de la caméra
        val rotationMatrix = FloatArray(16)
        Matrix.setIdentityM(rotationMatrix, 0)
        Matrix.rotateM(rotationMatrix, 0, cameraYaw, 0f, 1f, 0f)
        Matrix.rotateM(rotationMatrix, 0, cameraPitch, 1f, 0f, 0f)

        val adjustedViewMatrix = FloatArray(16)
        Matrix.multiplyMM(adjustedViewMatrix, 0, rotationMatrix, 0, viewMatrix, 0)

        // Calcul de la matrice MVP
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, adjustedViewMatrix, 0)

        // Rendu de la sphère
        sphere.drawWithVideoTexture(mvpMatrix, textureId)
    }

    /*override fun onDrawFrame(gl: GL10?) {
        // Effacer l'écran
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Calcul de la matrice de transformation
        val mvpMatrix = FloatArray(16)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Dessin de la sphère avec la texture vidéo
        sphere.drawWithVideoTexture(mvpMatrix, textureId)
    }*/

    private fun startProgressUpdates() {
        fixedRateTimer("progressUpdater", initialDelay = 0L, period = 1000L) {
            mediaPlayer?.let {
                progressUpdateListener?.invoke(it.currentPosition, it.duration)
            }
        }
    }

    fun updateRotation(dx: Float, dy: Float) {
        rotationX -= dx / 10f
        rotationY -= dy / 10f
        rotationY = rotationY.coerceIn(-85f, 85f)
    }

    fun updateCameraRotation(deltaYaw: Float, deltaPitch: Float) {
        cameraYaw += deltaYaw
        cameraPitch = (cameraPitch + deltaPitch).coerceIn(-85f, 85f) // Limiter l'angle de la caméra
    }

    fun setProgressListener(listener: (Int, Int) -> Unit) {
        progressUpdateListener = listener
    }

    fun setPlayingState(isPlaying: Boolean) {
        mediaPlayer?.let { // Utilisez le safe call pour éviter une exception
            if (isPlaying) {
                it.start()
            } else {
                it.pause()
            }
        }
    }

    fun getVideoProgress(): Pair<Int, Int> {
        return mediaPlayer?.let {
            Pair(it.currentPosition, it.duration)
        } ?: Pair(0, 1)
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getCameraOrientation(): Pair<Float, Float> {
        return Pair(cameraYaw, cameraPitch)
    }
}
