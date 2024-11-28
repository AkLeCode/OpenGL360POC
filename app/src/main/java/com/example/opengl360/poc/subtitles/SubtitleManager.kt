package com.example.opengl360.poc.subtitles

import android.content.Context
import android.text.Spanned
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.text.HtmlCompat
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 * Classe responsable de la gestion des sous-titres dans l'application.
 * Cette classe charge les sous-titres depuis un fichier .srt et les convertit
 * en une liste de `Subtitle`.
 *
 * @param context Le contexte Android pour accéder aux ressources.
 * @param subtitleColor La couleur que l'on appliquera uniquement aux portions de texte
 *                      qui sont dans une balise <b> ou qui ont une couleur définie.
 * @param subtitlePath Le chemin du fichier .srt contenant les sous-titres.
 */
class SubtitleManager(
    private val context: Context,
    private val subtitleColor: Color,
    private val subtitlePath: String
) {

    // Liste des sous-titres chargés
    val subtitles = mutableListOf<Subtitle>()

    init {
        loadSubtitles()
    }

    /**
     * Charge les sous-titres à partir du fichier .srt.
     */
    private fun loadSubtitles() {
        try {
            val file = File(subtitlePath)
            if (file.exists()) {
                val reader = BufferedReader(InputStreamReader(FileInputStream(file)))

                var line: String? = reader.readLine()
                while (line != null) {
                    try {
                        // Ligne contenant le timecode
                        val timeRange = reader.readLine()

                        // Lire toutes les lignes de texte jusqu'à la ligne vide
                        val text = StringBuilder()
                        var subtitleLine = reader.readLine()
                        while (!subtitleLine.isNullOrEmpty()) {
                            text.append(subtitleLine).append("\n")
                            subtitleLine = reader.readLine()
                        }

                        // Convertit la ligne "00:01:15,000 --> 00:01:20,000" en deux temps
                        val (start, end) = parseTimeRange(timeRange)

                        // Convertit le texte HTML en AnnotatedString
                        val formattedText = parseHtmlToAnnotatedString(text.toString().trim())

                        // Ajoute à la liste
                        subtitles.add(Subtitle(start, end, formattedText))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    line = reader.readLine()
                }
                reader.close()
            } else {
                Log.e("SubtitleManager", "Le fichier SRT n'existe pas au chemin : $subtitlePath")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SubtitleManager", "Erreur lors du chargement des sous-titres : ${e.message}")
        }
    }

    /**
     * Extrait deux durées (start et end) de la forme "00:01:15,000 --> 00:01:20,000".
     */
    private fun parseTimeRange(timeRange: String?): Pair<Int, Int> {
        if (timeRange.isNullOrEmpty()) return Pair(0, 0)
        val times = timeRange.split(" --> ")
        val startTime = convertTimeToMilliseconds(times[0])
        val endTime = convertTimeToMilliseconds(times[1])
        return Pair(startTime, endTime)
    }

    /**
     * Convertit un format de temps (hh:mm:ss,SSS) en millisecondes.
     */
    private fun convertTimeToMilliseconds(time: String): Int {
        val parts = time.split(",", ":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()
        val seconds = parts[2].toInt()
        val milliseconds = parts[3].toInt()
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + milliseconds
    }

    /**
     * Convertit un texte HTML en AnnotatedString.
     *
     * Règle demandée :
     * - Seul le texte **dans** un <b> ou ayant une couleur (balise <font color="...">)
     *   sera colorisé avec [subtitleColor].
     * - Le reste du texte aura une couleur par défaut (ici on utilise `Color.Unspecified`).
     * - On conserve également le style gras si présent, ou l'italique, etc. (facultatif, ajustable).
     */
    private fun parseHtmlToAnnotatedString(htmlText: String): AnnotatedString {
        val spanned: Spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val builder = AnnotatedString.Builder()

        for (index in spanned.indices) {
            val char = spanned[index]
            val spans = spanned.getSpans(index, index + 1, Any::class.java)

            // Par défaut, on n'applique pas de couleur particulière
            var colorToUse = Color.Unspecified

            // On va aussi gérer si le caractère doit être bold ou italic
            var fontWeight = FontWeight.Normal
            var fontStyle = FontStyle.Normal

            // On vérifie si ce caractère est sous un style "bold" ou sous une couleur
            spans.forEach { span ->
                when (span) {
                    is android.text.style.StyleSpan -> {
                        // Vérifier si c'est gras ou italique
                        when (span.style) {
                            android.graphics.Typeface.BOLD -> {
                                fontWeight = FontWeight.Bold
                                // On colorise aussi si c'est gras
                                colorToUse = subtitleColor
                            }
                            android.graphics.Typeface.ITALIC -> {
                                fontStyle = FontStyle.Italic
                                // (pour l'instant, on ne colorise pas juste pour l'italique,
                                //  mais tu peux le faire si tu le souhaites)
                            }
                        }
                    }
                    is android.text.style.ForegroundColorSpan -> {
                        // S'il y a un span de couleur, on colorise avec subtitleColor
                        colorToUse = subtitleColor
                    }
                }
            }

            // Maintenant, on applique ce style à 1 caractère
            builder.addStyle(
                style = SpanStyle(
                    color = colorToUse,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle
                ),
                start = builder.length,
                end = builder.length + 1
            )
            builder.append(char)
        }

        return builder.toAnnotatedString()
    }

    /**
     * Récupère le sous-titre pour un temps donné.
     */
    fun getSubtitleForTime(currentTime: Int): AnnotatedString? {
        return subtitles.firstOrNull { currentTime in it.startTime..it.endTime }?.text
    }
}
