package com.example.track_me_mobile.features.auth.domain.models

data class CsrfToken(
    val token: String,
    val parameterName: String
)