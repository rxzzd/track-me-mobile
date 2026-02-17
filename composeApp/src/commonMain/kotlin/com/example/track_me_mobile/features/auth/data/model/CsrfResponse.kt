package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CsrfResponse(
    val token: String,
    val headerName: String,      // "X-CSRF-TOKEN" — использовать в заголовке запроса
    val parameterName: String    // "_csrf" — для форм, нам не нужен
)