package com.example.track_me_mobile.features.users.domain.models

data class AdminUser(
    val id: String,
    val fullName: String,
    val telegramNick: String,
    val isConfirmed: Boolean
)