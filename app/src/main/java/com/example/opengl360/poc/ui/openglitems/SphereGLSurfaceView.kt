package com.example.opengl360.poc.ui.openglitems

import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent

class SphereGLSurfaceView(context: Context) : GLSurfaceView(context) {
    val renderer: SphereRenderer
    var isInit: Boolean = false

    init {
        setEGLConfigChooser(8, 8, 8, 8, 16, 4)
        // Configure OpenGL ES 3.0
        setEGLContextClientVersion(3)
        renderer = SphereRenderer(context)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        setZOrderMediaOverlay(true)
        holder.setFormat(PixelFormat.TRANSLUCENT)

        // Gestion tactile pour les rotations
        var previousX = 0f
        var previousY = 0f

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - previousX
                    val dy = event.y - previousY
                    renderer.updateRotation(dx, dy)
                    requestRender() // Redessiner après interaction
                }
            }
            previousX = event.x
            previousY = event.y
            true
        }
        isInit = true
    }

    fun getVideoProgress(): Pair<Int, Int> {
        return renderer.getVideoProgress()
    }

    fun updatePlayingState(isPlaying: Boolean) {
        renderer.setPlayingState(isPlaying)
    }

    fun seekTo(position: Int) {
        renderer.seekTo(position)
    }

    fun getCameraOrientation(): Pair<Float, Float> {
        return renderer.getCameraOrientation()
    }
}