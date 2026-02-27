package com.example.track_me_mobile.features.team_card.domain

import com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.domain.models.TeamCard

interface TeamCardRepository {
    // ── Просмотр ──────────────────────────────────────────────────────────
    suspend fun getTeamById(id: String): Result<TeamCard>
    suspend fun getTrackerFullName(username: String): Result<String>

    // ── Данные для формы создания ─────────────────────────────────────────
    suspend fun getTrackers(): Result<List<TrackerUser>>
    suspend fun getStreams(): Result<List<Stream>>
    suspend fun getNtiMarkets(): Result<List<NtiMarket>>

    // ── Создание ──────────────────────────────────────────────────────────
    suspend fun createTeam(request: CreateTeamRequest): Result<Unit>
}