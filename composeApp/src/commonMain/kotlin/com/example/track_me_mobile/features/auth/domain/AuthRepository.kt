package com.example.track_me_mobile.features.auth.domain

import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.features.auth.domain.models.CsrfToken

interface AuthRepository {
    // Получение CSRF токена
    suspend fun getCsrfToken(): Result<CsrfToken>

    // Вход
    suspend fun login(username: String, password: String, csrf: CsrfToken): Result<Unit>

    suspend fun getUserInfo(): Result<UserInfo>

    suspend fun syncSession(cookieString: String)
}