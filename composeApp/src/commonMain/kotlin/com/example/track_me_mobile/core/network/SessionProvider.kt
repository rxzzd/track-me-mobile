package com.example.track_me_mobile.core.network

interface SessionProvider {
    suspend fun get(): String?
    suspend fun clear()
}
