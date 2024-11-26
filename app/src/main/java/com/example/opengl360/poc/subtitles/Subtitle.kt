package com.example.opengl360.poc.subtitles

import androidx.compose.ui.text.AnnotatedString

/**
 * Classe représentant un sous-titre dans une vidéo.
 *
 * @property startTime Le temps de début du sous-titre, en millisecondes.
 * @property endTime Le temps de fin du sous-titre, en millisecondes.
 * @property text Le texte du sous-titre, formaté en AnnotatedString pour supporter le style (gras, italique, couleur, etc.).
 */
data class Subtitle(
    val startTime: Int, // Temps de début du sous-titre en millisecondes
    val endTime: Int,   // Temps de fin du sous-titre en millisecondes
    val text: AnnotatedString // Texte stylé du sous-titre
)