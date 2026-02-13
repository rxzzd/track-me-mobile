package com.example.track_me_mobile.features.auth.domain

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Boolean>
}