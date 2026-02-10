package com.example.nexus11.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Long = 0,
    val isEdited: Boolean = false,
    val replyToText: String? = null,
    val reaction: String? = null,
    val status: String = "sent" // ✅ ESTE CAMPO ES OBLIGATORIO PARA QUE NO DE ERROR
)