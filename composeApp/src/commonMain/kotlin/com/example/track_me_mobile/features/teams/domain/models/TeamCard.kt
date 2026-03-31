package com.example.track_me_mobile.features.teams.domain.models

data class NtiMarket(
    val id: String,
    val name: String,
    val displayName: String
)

data class Stream(
    val id: String,
    val name: String,
    val description: String,
    val active: Boolean,
    val startDate: String? = null,
    val endDate: String? = null
)

// Доменная модель — только то что нужно UI, без лишних полей с сервера
data class TeamCard(
    val id: String,
    val name: String,
    val meetingRoomLink: String,
    val description: String,
    val status: String,
    val username: String,
    val enabled: Boolean,
    val ntiMarkets: List<NtiMarket>,
    val readinessLevel: String,
    val averageGrade: Double?,
    val stream: Stream?,
    val meetingsCount: Int,
    val meetingsCompletedCount: Int,
    val meetingsNotHappenedCount: Int,
    val teamsCount: Int? = null,
)