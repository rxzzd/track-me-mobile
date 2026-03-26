package com.example.track_me_mobile.features.team_card.domain.models

data class UpdateTeamRequest(
    val teamId: String,
    val name: String,
    val meetingRoomLink: String,
    val description: String,
    val ntiMarketIds: List<String>,
    val readinessLevel: String,
    // Только для ADMIN/SUPER_ADMIN
    val trackerUsername: String? = null,
    val streamId: String? = null,
)