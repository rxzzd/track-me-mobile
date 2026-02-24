package com.example.track_me_mobile.features.auth.domain

interface RegistrationRepository {
    suspend fun initRegistration(
        username: String,
        password: String,
        phoneNumber: String,
        fullName: String,
        email: String,
        role: String
    ): Result<Unit>
}