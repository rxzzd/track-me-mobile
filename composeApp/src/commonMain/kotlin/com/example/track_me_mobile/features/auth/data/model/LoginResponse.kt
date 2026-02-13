package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val userId: String
)