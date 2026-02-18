package com.example.track_me_mobile.features.profile.domain.models

data class UserProfile(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val avatarUrl: String?,
    val roles: List<String>,
    val enabled: Boolean
)