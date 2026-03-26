package com.example.track_me_mobile.features.team_card.domain.models

data class CreateTeamRequest(
    val name: String,
    val meetingRoomLink: String,
    val description: String,
    val trackerUsername: String,
    val streamId: String,
    val ntiMarketIds: List<String>,
    val readinessLevel: String
)