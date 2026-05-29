package com.example.track_me_mobile.features.auth.domain

interface AuthSessionSaver {
    suspend fun saveSession(cookieString: String)
}
