package com.example.track_me_mobile.features.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfoDto(
    val id: String? = null,
    val username: String,
    val roles: List<String>,
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val avatarUrl: String? = null,
    val enabled: Boolean
)