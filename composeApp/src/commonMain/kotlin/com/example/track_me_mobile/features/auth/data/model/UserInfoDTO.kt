package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDto(
    val roles: List<String> // Сервер обычно шлет строки ["ADMIN", "TRACKER"]
)