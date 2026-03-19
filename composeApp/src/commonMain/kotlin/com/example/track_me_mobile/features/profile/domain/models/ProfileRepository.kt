package com.example.track_me_mobile.features.profile.domain

import com.example.track_me_mobile.features.profile.domain.models.UserProfile

interface ProfileRepository {
    suspend fun getAccountInfo(): Result<UserProfile>
    suspend fun updateAccount(
        fullName: String,
        email: String,
        phoneNumber: String,
        avatarUrl: String?
    ): Result<Unit>
}