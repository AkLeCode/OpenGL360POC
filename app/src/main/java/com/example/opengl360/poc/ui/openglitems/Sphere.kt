package com.example.opengl360.poc.ui.openglitems

import android.opengl.GLES11Ext
import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Cette classe représente une sphère 3D pour OpenGL ES.
 * Elle génère les coordonnées des sommets, les indices et les coordonnées de texture nécessaires.
 *
 * @param latSegments Nombre de segments en latitude
 * @param lonSegments Nombre de segments en longitude
 * @param radius Rayon de la sphère
 */
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

        // Génération des sommets et des coordonnées de texture
        for (lat in 0..latSegments) {
            val theta = lat * PI / latSegments
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (lon in 0..lonSegments) {
                val phi = lon * 2 * PI / lonSegments
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = (cosPhi * sinTheta).toFloat()
                val y = cosTheta.toFloat()
                val z = (sinPhi * sinTheta).toFloat()
                vertices.add(x * radius)
                vertices.add(y * radius)
                vertices.add(z * radius)

                texCoords.add(lon.toFloat() / lonSegments)
                texCoords.add(lat.toFloat() / latSegments)
            }
        }

        // Génération des indices pour les triangles
        for (lat in 0 until latSegments) {
            for (lon in 0 until lonSegments) {
                val first = (lat * (lonSegments + 1) + lon).toShort()
                val second = (first + lonSegments + 1).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())
            }
        }

        // Création des buffers
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

        // Shader de vertex
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

        // Shader de fragment
        val fragmentShaderCode = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES uTexture;
            varying vec2 vTexCoord;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """.trimIndent()

        // Compilation des shaders
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Création du programme OpenGL
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

    /**
     * Dessine la sphère avec une texture vidéo.
     *
     * @param mvpMatrix Matrice de transformation
     * @param textureId Identifiant de la texture
     */
    fun drawWithVideoTexture(mvpMatrix: FloatArray, textureId: Int) {
        GLES30.glUseProgram(program)

        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(texCoordHandle)
        GLES30.glVertexAttribPointer(texCoordHandle, 2, GLES30.GL_FLOAT, false, 0, texCoordBuffer)

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glUniform1i(textureHandle, 0)

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexCount, GLES30.GL_UNSIGNED_SHORT, indexBuffer)

        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(texCoordHandle)
    }

    /**
     * Charge et compile un shader OpenGL.
     *
     * @param type Type du shader (vertex ou fragment)
     * @param shaderCode Code source du shader
     * @return Identifiant du shader compilé
     */
    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)

            // Vérification des erreurs de compilation
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val errorMsg = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                throw RuntimeException("Erreur de compilation du shader : $errorMsg")
            }
        }
    }
}
