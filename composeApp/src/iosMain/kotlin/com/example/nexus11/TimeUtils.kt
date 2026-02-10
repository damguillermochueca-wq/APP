package com.example.nexus11

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

// ✅ Cumplimos la promesa para iOS (iPhone)
actual fun getCurrentTimeMillis(): Long {
    // Multiplicamos por 1000 porque iOS da segundos y queremos milisegundos
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}