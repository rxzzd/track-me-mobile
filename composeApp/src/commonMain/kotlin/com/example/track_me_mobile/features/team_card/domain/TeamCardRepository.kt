package com.example.track_me_mobile.features.team_card.domain

import com.example.track_me_mobile.features.teams.domain.models.TeamCard

interface TeamCardRepository {
    suspend fun getTeamById(id: String): Result<TeamCard>
    suspend fun getTrackerFullName(username: String): Result<String>
}