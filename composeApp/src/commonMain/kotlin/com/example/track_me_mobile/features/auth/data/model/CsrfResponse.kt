package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CsrfResponse(
    val token: String,
    val parameterName: String
)