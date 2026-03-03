package com.example.track_me_mobile.features.teams.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NtiMarketDto(
    val id: String,
    val name: String,
    val displayName: String
)

@Serializable
data class StreamDto(
    val id: String,
    val name: String,
    val startDate: String? = null,
    val endDate: String? = null,
    val description: String,
    val active: Boolean
)

@Serializable
data class TeamCardDto(
    val id: String,
    val name: String,
    val description: String,
    val status: String,
    val username: String,
    val enabled: Boolean,
    val ntiMarkets: List<NtiMarketDto> = emptyList(),
    val readinessLevel: String = "",
    val averageGrade: Double? = null,
    val streams: List<StreamDto> = emptyList(),  // ← было StreamDto?, стало List<StreamDto>
    val meetingsCount: Int = 0,
    val meetingsCompletedCount: Int = 0,
    val meetingsNotHappenedCount: Int = 0
)

@Serializable
data class TeamCardsPageDto(
    val content: List<TeamCardDto>
)