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

    private var progressUpdateListener: ((Int, Int) -> Unit)? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // Définit un fond blanc RGBA
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

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
        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 90f, aspectRatio, 0.1f, 100f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        surfaceTexture.updateTexImage()

        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f)
        Matrix.rotateM(viewMatrix, 0, rotationX, 0f, 1f, 0f)
        Matrix.rotateM(viewMatrix, 0, rotationY, 1f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        sphere.drawWithVideoTexture(modelViewProjectionMatrix, textureId)
    }

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
        return Pair(rotationX, rotationY) // Rotation sur X et Y
    }
}
