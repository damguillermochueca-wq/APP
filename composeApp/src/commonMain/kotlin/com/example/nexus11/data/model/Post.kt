package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatarUrl: String? = null,
    val imageUrl: String? = null,
    val description: String = "",
    val timestamp: Long = 0L,

    // Mantenemos esto por si tienes posts antiguos y que no pete la app
    val likes: Int = 0,

    // ✅ NUEVO: Control estricto de 1 Like por persona
    val likedBy: Map<String, Boolean> = emptyMap(),

    // ✅ NUEVO: Mapa de comentarios para poder leerlos
    val comments: Map<String, String> = emptyMap()
)