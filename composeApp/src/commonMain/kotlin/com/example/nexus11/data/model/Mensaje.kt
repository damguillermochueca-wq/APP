import kotlinx.serialization.Serializable

@Serializable
data class Mensaje(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "", // ✅ FUNDAMENTAL: Para mostrar quién envía el mensaje en la lista de chats
    val timestamp: Long = 0,
    val isEdited: Boolean = false,
    val replyToText: String? = null,
    val reaction: String? = null,
    val status: String = "sent",
    val imageUrl: String? = null, // 📸 OPCIONAL: Por si quieres enviar fotos por chat más adelante
    val readBy: Map<String, Boolean> = emptyMap() // 👀 OPCIONAL: Para el "visto" o doble check
)