package com.example.track_me_mobile.features.team_card.data.models

import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import kotlinx.serialization.Serializable

// ── NTI Markets response ──────────────────────────────────────────────────────

@Serializable
data class NtiMarketDto(
    val id: String,
    val name: String,
    val displayName: String? = null
) {
    fun toDomain() = NtiMarket(
        id          = id,
        name        = name,
        displayName = displayName ?: name
    )
}

// ── Create team request body ──────────────────────────────────────────────────

@Serializable
data class CreateTeamRequestDto(
    val name: String,
    val meetingRoomLink: String,
    val description: String,
    val ntiMarketIds: List<String>,
    val readinessLevel: String
)