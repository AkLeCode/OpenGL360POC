package com.example.opengl360.poc.subtitles

import android.content.Context
import android.text.Spanned
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.text.HtmlCompat
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Classe responsable de la gestion des sous-titres dans l'application.
 * Cette classe charge les sous-titres depuis un fichier .srt et les convertit en une liste de `Subtitle`.
 *
 * @param context Le contexte Android pour accéder aux ressources.
 * @param subtitleResId L'ID de ressource du fichier .srt contenant les sous-titres.
 */
class SubtitleManager(private val context: Context, private val subtitleResId: Int) {

    // Liste des sous-titres chargés
    val subtitles = mutableListOf<Subtitle>()

    // Initialisation : charger les sous-titres dès la création de l'objet
    init {
        loadSubtitles()
    }

    /**
     * Charge les sous-titres à partir du fichier .srt.
     * Cette méthode lit le fichier ligne par ligne, extrait les informations des sous-titres
     * (temps de début, temps de fin, texte), et les stocke dans une liste.
     */
    private fun loadSubtitles() {
        val inputStream = context.resources.openRawResource(subtitleResId)
        val reader = BufferedReader(InputStreamReader(inputStream))

        var line: String? = reader.readLine()
        while (line != null) {
            try {
                // Ligne contenant l'index du sous-titre
                val index = line.toInt()

                // Ligne contenant les temps de début et de fin
                val timeRange = reader.readLine()
                val text = StringBuilder()

                // Lire le texte du sous-titre, ligne par ligne
                var subtitleLine = reader.readLine()
                while (!subtitleLine.isNullOrEmpty()) {
                    text.append(subtitleLine).append("\n")
                    subtitleLine = reader.readLine()
                }

                // Convertir les temps de début et de fin
                val (start, end) = parseTimeRange(timeRange)

                // Convertir le texte en AnnotatedString (avec styles HTML)
                val formattedText = parseHtmlToAnnotatedString(text.toString().trim())

                // Ajouter le sous-titre à la liste
                subtitles.add(Subtitle(start, end, formattedText))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            line = reader.readLine()
        }
    }

    /**
     * Parse une ligne contenant une plage de temps pour extraire les temps de début et de fin.
     *
     * @param timeRange La ligne contenant la plage de temps (exemple : "00:01:15,000 --> 00:01:20,000").
     * @return Une paire contenant le temps de début et le temps de fin en millisecondes.
     */
    private fun parseTimeRange(timeRange: String): Pair<Int, Int> {
        val times = timeRange.split(" --> ")
        val startTime = convertTimeToMilliseconds(times[0])
        val endTime = convertTimeToMilliseconds(times[1])
        return Pair(startTime, endTime)
    }

    /**
     * Convertit un format de temps (hh:mm:ss,SSS) en millisecondes.
     *
     * @param time La chaîne représentant le temps (exemple : "00:01:15,000").
     * @return Le temps en millisecondes.
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
     * Convertit un texte avec balises HTML en AnnotatedString en préservant les styles.
     *
     * @param htmlText Le texte avec balises HTML.
     * @return Un AnnotatedString contenant le texte stylé.
     */
    private fun parseHtmlToAnnotatedString(htmlText: String): AnnotatedString {
        val spanned: Spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val builder = AnnotatedString.Builder()

        for (index in spanned.indices) {
            val char = spanned[index]
            val spans = spanned.getSpans(index, index + 1, Any::class.java)
            spans.forEach { span ->
                when (span) {
                    is android.text.style.StyleSpan -> {
                        if (span.style == android.graphics.Typeface.BOLD) {
                            builder.addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                start = index,
                                end = index + 1
                            )
                        } else if (span.style == android.graphics.Typeface.ITALIC) {
                            builder.addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                start = index,
                                end = index + 1
                            )
                        }
                    }
                    is android.text.style.ForegroundColorSpan -> {
                        builder.addStyle(
                            SpanStyle(color = Color(span.foregroundColor)),
                            start = index,
                            end = index + 1
                        )
                    }
                }
            }
            builder.append(char)
        }

        return builder.toAnnotatedString()
    }

    /**
     * Récupère le sous-titre pour un moment donné.
     *
     * @param currentTime Le temps actuel dans la vidéo, en millisecondes.
     * @return Le sous-titre correspondant au moment donné, ou null s'il n'y en a pas.
     */
    fun getSubtitleForTime(currentTime: Int): AnnotatedString? {
        val text = subtitles.firstOrNull { currentTime in it.startTime..it.endTime }?.text
        return text
    }
}
