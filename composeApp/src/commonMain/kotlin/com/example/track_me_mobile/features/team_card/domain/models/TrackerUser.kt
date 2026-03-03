package com.example.track_me_mobile.features.team_card.domain.models

data class TrackerUser(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val avatarUrl: String?
)