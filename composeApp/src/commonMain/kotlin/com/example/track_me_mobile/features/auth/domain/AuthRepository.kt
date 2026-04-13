package com.example.track_me_mobile.features.auth.domain

import com.example.track_me_mobile.core.domain.models.UserInfo

interface AuthRepository {
    suspend fun getUserInfo(): Result<UserInfo>
}