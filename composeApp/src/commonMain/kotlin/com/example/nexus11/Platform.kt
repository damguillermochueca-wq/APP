package com.example.nexus11

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform