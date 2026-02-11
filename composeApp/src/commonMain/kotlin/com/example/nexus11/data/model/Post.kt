package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    // ✅ AÑADIDO: Ahora guardamos la foto del usuario en el post
    val userAvatarUrl: String? = null,
    val imageUrl: String?= null,
    val description: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val comments: Map<String, String> = emptyMap()
)