package com.example.opengl360.poc.sphere

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
import kotlin.math.cos
import kotlin.math.sin

/**
 * Classe SphereRenderer utilisée pour rendre une vidéo projetée sur une sphère 360° à l'aide d'OpenGL.
 *
 * @param context Contexte de l'application utilisé pour accéder aux ressources comme la vidéo.
 */
class SphereRenderer(private val context: Context) : GLSurfaceView.Renderer {
    // Objet représentant la sphère
    private lateinit var sphere: Sphere
    // SurfaceTexture pour gérer la texture vidéo
    private lateinit var surfaceTexture: SurfaceTexture
    // MediaPlayer pour lire la vidéo
    private var mediaPlayer: MediaPlayer? = null
    // ID de la texture utilisée pour la vidéo
    private var textureId: Int = 0

    // Matrices de transformation
    private val projectionMatrix = FloatArray(16) // Matrice de projection
    private val viewMatrix = FloatArray(16) // Matrice de vue
    private val modelViewProjectionMatrix = FloatArray(16) // Matrice combinée

    // Variables de rotation, modifier pour ajuster la valeur par défaut.
    private var rotationYaw = 180f // Rotation horizontale (Yaw)
    private var rotationPitch = 0f // Rotation verticale (Pitch)

    /**
     * Méthode appelée lors de la création de la surface OpenGL.
     */
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Définit la couleur de fond en blanc et active le test de profondeur
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)

        // Configure les paramètres de la texture vidéo
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        // Initialise la sphère
        sphere = Sphere(100, 100, 1f)

        // Crée la texture vidéo
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        surfaceTexture = SurfaceTexture(textureId)

        // Configure le MediaPlayer pour lire la vidéo
        mediaPlayer = MediaPlayer.create(context, R.raw.bundle)
        mediaPlayer?.setSurface(Surface(surfaceTexture))
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()
    }

    /**
     * Méthode appelée lorsque les dimensions de la surface OpenGL changent.
     *
     * @param gl Le contexte OpenGL.
     * @param width Largeur de la surface.
     * @param height Hauteur de la surface.
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Définit le viewport OpenGL
        GLES30.glViewport(0, 0, width, height)
        // Calcul du ratio d'aspect
        val aspectRatio = width.toFloat() / height.toFloat()
        // Configure une matrice de projection perspective avec un champ de vision (FOV) réduit
        val fovY = 40f // Réduction pour diminuer l'effet "fish-eye"
        Matrix.perspectiveM(projectionMatrix, 0, fovY, aspectRatio, 0.1f, 100f)
    }

    /**
     * Méthode appelée pour dessiner la scène à chaque frame.
     */
    override fun onDrawFrame(gl: GL10?) {
        // Efface l'écran avec la couleur de fond et le tampon de profondeur
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // Met à jour le contenu de la texture vidéo
        surfaceTexture.updateTexImage()

        // Calcul des coordonnées de la caméra en fonction de Yaw et Pitch
        val radius = 1f // Rayon constant
        val eyeX = radius * cos(Math.toRadians(rotationPitch.toDouble())) * sin(Math.toRadians(rotationYaw.toDouble())).toFloat()
        val eyeY = radius * sin(Math.toRadians(rotationPitch.toDouble())).toFloat()
        val eyeZ = radius * cos(Math.toRadians(rotationPitch.toDouble())) * cos(Math.toRadians(rotationYaw.toDouble())).toFloat()

        // Position de la caméra
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f
        val upX = 0f
        val upY = 1f
        val upZ = 0f

        // Met à jour la matrice de vue
        Matrix.setLookAtM(viewMatrix, 0, eyeX.toFloat(), eyeY, eyeZ.toFloat(), centerX, centerY, centerZ, upX, upY, upZ)
        // Combine les matrices de vue et de projection
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Dessine la sphère avec la texture vidéo
        sphere.drawWithVideoTexture(modelViewProjectionMatrix, textureId)
    }

    /**
     * Met à jour les angles de rotation de la caméra.
     *
     * @param deltaYaw Déplacement horizontal.
     * @param deltaPitch Déplacement vertical.
     */
    fun updateRotation(deltaYaw: Float, deltaPitch: Float) {
        val sensitivity = 0.1f // Sensibilité des rotations
        rotationYaw = (rotationYaw + deltaYaw * sensitivity) % 360f
        rotationPitch = (rotationPitch - deltaPitch * sensitivity).coerceIn(-75f, 75f)
    }

    /**
     * Définit l'état de lecture de la vidéo.
     *
     * @param isPlaying `true` pour lire, `false` pour mettre en pause.
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
     * Récupère la progression actuelle et la durée totale de la vidéo.
     *
     * @return Un `Pair` contenant la position actuelle et la durée totale.
     */
    fun getVideoProgress(): Pair<Int, Int> {
        return mediaPlayer?.let {
            Pair(it.currentPosition, it.duration)
        } ?: Pair(0, 1)
    }

    /**
     * Cherche une position spécifique dans la vidéo.
     *
     * @param position Position à laquelle chercher, en millisecondes.
     */
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    /**
     * Retourne les angles de rotation actuels de la caméra.
     *
     * @return Un `Pair` contenant Yaw et Pitch.
     */
    fun getCameraOrientation(): Pair<Float, Float> {
        return Pair(rotationYaw, rotationPitch)
    }
}
