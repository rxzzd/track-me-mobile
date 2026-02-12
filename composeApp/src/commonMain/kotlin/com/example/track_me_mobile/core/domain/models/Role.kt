package com.example.track_me_mobile.core.domain.models

enum class Role {
    TRACKER,
    ADMIN,
    SUPER_ADMIN
}

data class CurrentUser (
    val id: String,
    val username: String,
    val role: List<Role>,
    val fullname: String,
    val email: String,
    val phoneNumber: String,
    val avatarUrl: String,
    val enabled: Boolean
)
