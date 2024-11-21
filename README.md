# OpenGL 360° POC

## 📖 Description

Ce projet est un **Proof of Concept (POC)** développé pour expérimenter et tester les capacités d'OpenGL ES 3.0 dans la création d'une visualisation immersive de vidéos 360° dans une application Android.

L'objectif principal de ce projet est de rendre une vidéo 360° sur une sphère interactive et de fournir des fonctionnalités basiques de lecture, contrôle, et interaction avec une vidéo.

Le projet utilise **Jetpack Compose** pour la conception de l'interface utilisateur, tout en intégrant un rendu OpenGL pour la sphère.

---

## 🚀 Fonctionnalités

### 🌐 Rendu 360° avec OpenGL
- Utilisation d'une sphère pour projeter une vidéo 360° à l'intérieur.
- Prise en charge de la navigation par **gestes** (panorama horizontal et vertical).
- Retour en temps reel de la position.

### 🎥 Contrôle de la vidéo
- **Lecture/Pause** de la vidéo.
- **Barre de progression** pour avancer ou reculer dans la vidéo.
- Mise à jour en temps réel de la progression et de la durée de la vidéo.

### ⚙️ Interaction utilisateur
- Mouvement de la caméra par glissement vertical et horizontal.
- Contrôle limité pour éviter les effets indésirables (ex. : roll ou inclinaison latérale).
- Mode **plein écran** activable pour une immersion totale.

### 🔧 Techniques utilisées
- **OpenGL ES 3.0** pour le rendu graphique.
- **Shaders personnalisés** pour optimiser la qualité visuelle.
- **Jetpack Compose** pour l'interface utilisateur moderne et réactive.

---

## 📦 Structure du projet

### Fichiers principaux
1. **`Sphere.kt`**  
   Définit la géométrie de la sphère et les shaders OpenGL pour appliquer la texture vidéo.

2. **`SphereRenderer.kt`**  
   Responsable de la gestion du rendu OpenGL, des mouvements de la caméra, et de la liaison avec la vidéo.

3. **`SphereGLSurfaceView.kt`**  
   Vue OpenGL personnalisée utilisée comme conteneur pour le rendu.

4. **`MainScreen.kt`**  
   Interface utilisateur Jetpack Compose pour afficher et contrôler la scène 360°.

---

## 📋 Limitations
- Ce projet est un **POC** et n'est pas optimisé pour un usage en production.
- Les shaders et le rendu sphérique peuvent encore être améliorés pour une meilleure qualité visuelle.
- La vidéo 360° doit être correctement formatée pour un rendu optimal.

---

## 📜 Licence
Ce projet est open-source et peut être utilisé comme base pour vos propres expériences. Cependant, il est fourni "tel quel", sans garantie d'aucune sorte.

---
## Video
Video d'origine : https://vimeo.com/214401712

