package com.example.track_me_mobile.features.auth.domain

import com.example.track_me_mobile.features.auth.domain.models.CsrfToken

interface AuthRepository {
    // Получение CSRF токена
    suspend fun getCsrfToken(): Result<CsrfToken>

    // Вход
    suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit>
}