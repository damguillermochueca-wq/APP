package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String? = null,
    val imageUrl: String = "",
    val timestamp: Long = 0
)