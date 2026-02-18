package com.example.track_me_mobile.features.profile.domain.models

data class UserProfile(
    val fullName: String,
    val email: String,
    val phone: String,
    val telegram: String,
    val role: String
)