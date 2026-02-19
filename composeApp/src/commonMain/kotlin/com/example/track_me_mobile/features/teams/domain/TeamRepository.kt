package com.example.track_me_mobile.features.teams.domain

import com.example.track_me_mobile.features.teams.domain.models.TeamCard

interface TeamRepository {
    suspend fun getTeamCards(
        streamId: String? = null,
        page: Int = 0,
        size: Int = 100
    ): Result<List<TeamCard>>
}