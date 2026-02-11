package com.example.track_me_mobile

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform