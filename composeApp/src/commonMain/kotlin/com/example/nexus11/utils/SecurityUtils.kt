package com.example.nexus11.utils

import io.ktor.utils.io.core.toByteArray
import org.kotlincrypto.hash.sha2.SHA256

object SecurityUtils {
    // Convierte "hola123" en "b2f5ff47436671b6e533d8dc3614845d..."
    @OptIn(ExperimentalStdlibApi::class)
    fun hashPassword(password: String): String {
        val digest = SHA256().digest(password.toByteArray())
        return digest.toHexString()
    }
}