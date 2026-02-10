package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatarUrl: String? = null,
    val imageUrl: String = "", // Las historias siempre tienen foto
    val timestamp: Long = 0
)