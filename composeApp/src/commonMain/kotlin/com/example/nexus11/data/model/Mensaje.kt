package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Mensaje(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val imageUrl: String? = null,
    val replyToId: String? = null,
    val replyToText: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val isEdited: Boolean = false,
    // ✅ NUEVO: Estado de lectura
    val isRead: Boolean = false
)