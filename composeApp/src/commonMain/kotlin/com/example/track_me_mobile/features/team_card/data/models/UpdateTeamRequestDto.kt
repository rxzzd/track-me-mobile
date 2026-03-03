package com.example.track_me_mobile.features.team_card.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTeamRequestDto(
    val name: String,
    val description: String,
    val ntiMarketIds: List<String>,
    val readinessLevel: String
)