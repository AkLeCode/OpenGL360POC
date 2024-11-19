package com.example.opengl360.poc.ui.openglitems

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import android.opengl.GLES11Ext
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

class Sphere(latSegments: Int, lonSegments: Int, radius: Float) {
    private val vertexBuffer: FloatBuffer
    private val texCoordBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    private val program: Int
    private val positionHandle: Int
    private val texCoordHandle: Int
    private val mvpMatrixHandle: Int
    private val textureHandle: Int

    init {
        val vertices = mutableListOf<Float>()
        val texCoords = mutableListOf<Float>()
        val indices = mutableListOf<Short>()

        val vertsPerRow = lonSegments + 1

        // Augmenter le nombre de segments pour une sphère plus lisse
        val adjustedLatSegments = latSegments * 2
        val adjustedLonSegments = lonSegments * 2

        for (lat in 0..adjustedLatSegments) {
            val theta = lat * PI / adjustedLatSegments
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..adjustedLonSegments) {
                val phi = lon * 2 * PI / adjustedLonSegments - PI / 2 // Décalage pour centrer la texture
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = (cosPhi * sinTheta).toFloat()
                val y = cosTheta.toFloat()
                val z = (sinPhi * sinTheta).toFloat()
                vertices.add(x * radius)
                vertices.add(y * radius)
                vertices.add(z * radius)

                // Génération des coordonnées de texture avec une projection adaptée
                val u = lon.toFloat() / adjustedLonSegments
                val v = lat.toFloat() / adjustedLatSegments

                // Appliquer une correction pour réduire la distorsion aux pôles
                val correctedV = 0.5f - (asin(y) / PI)
                texCoords.add(u)
                texCoords.add(correctedV.toFloat())
            }
        }

        for (lat in 0 until adjustedLatSegments) {
            for (lon in 0 until adjustedLonSegments) {
                val first = (lat * (adjustedLonSegments + 1) + lon).toShort()
                val second = (first + adjustedLonSegments + 1).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())
            }
        }

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertices.toFloatArray())
        vertexBuffer.position(0)

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(texCoords.toFloatArray())
        texCoordBuffer.position(0)

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2)
            .order(ByteOrder.nativeOrder())
            .asShortBuffer()
            .put(indices.toShortArray())
        indexBuffer.position(0)

        indexCount = indices.size

        // Shader de vertex (inchangé)
        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec3 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                vTexCoord = aTexCoord;
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
            }
        """.trimIndent()

        // Shader de fragment (inchangé)
        val fragmentShaderCode = """
            #extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES uTexture;
uniform vec2 texelSize; // Taille du pas de la texture (à transmettre depuis Kotlin)
varying vec2 vTexCoord;

void main() {
    vec4 color = vec4(0.0);
    color += texture2D(uTexture, vTexCoord) * 0.5; // Central
    color += texture2D(uTexture, vTexCoord + texelSize * vec2(1.0, 0.0)) * 0.125; // Droite
    color += texture2D(uTexture, vTexCoord + texelSize * vec2(-1.0, 0.0)) * 0.125; // Gauche
    color += texture2D(uTexture, vTexCoord + texelSize * vec2(0.0, 1.0)) * 0.125; // Haut
    color += texture2D(uTexture, vTexCoord + texelSize * vec2(0.0, -1.0)) * 0.125; // Bas
    gl_FragColor = color;
}
        """.trimIndent()

        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
        }

        positionHandle = GLES30.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES30.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES30.glGetUniformLocation(program, "uTexture")
    }

    fun drawWithVideoTexture(mvpMatrix: FloatArray, textureId: Int) {
        GLES30.glUseProgram(program)

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)

        // Activer le mipmapping
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, indexBuffer)

        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val errorMsg = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Erreur de compilation du shader : $errorMsg")
        }

        return shader
    }
}
