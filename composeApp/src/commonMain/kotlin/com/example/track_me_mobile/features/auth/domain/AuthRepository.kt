package com.example.track_me_mobile.features.auth.domain

import com.example.track_me_mobile.core.domain.models.UserInfo

// Domain-интерфейс описывает только то, что реально используется.
// Детали OAuth (CSRF, сессии, коды) — это забота data-слоя, не domain.
interface AuthRepository {
    suspend fun getUserInfo(): Result<UserInfo>
}