package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val username: String,
    val password: String,
    val phoneNumber: String,
    val fullName: String,
    val email: String,
    val role: String
)