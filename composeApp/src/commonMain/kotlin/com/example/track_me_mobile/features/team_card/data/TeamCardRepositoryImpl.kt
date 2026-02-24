package com.example.track_me_mobile.features.team_card.data

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*

class TeamCardRepositoryImpl(
    private val client: HttpClient,
    private val userInfoHolder: UserInfoHolder
) : TeamCardRepository {

    private suspend fun fetchCsrf(): CsrfResponse =
        client.get(ApiConstants.CSRF_ENDPOINT) {
            header(HttpHeaders.Accept, "application/json")
        }.body()

    override suspend fun getTeamById(id: String): Result<TeamCard> {
        return try {
            println("[TEAM_CARD_REPO] getTeamById: start, id=$id, role=${userInfoHolder.userInfo?.mainRole}")
            println("[TEAM_CARD_REPO] getTeamById: endpoint=${ApiConstants.TEAM_CARD}")

            val response = client.get(ApiConstants.TEAM_CARD) {
                header(HttpHeaders.Accept, "application/json")
                parameter("id", id)
            }
            println("[TEAM_CARD_REPO] getTeamById: response status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                println("[TEAM_CARD_REPO] getTeamById: non-OK status, body=${response.bodyAsText()}")
                return Result.failure(Exception("Status: ${response.status}"))
            }

            var team = response.body<com.example.track_me_mobile.features.teams.data.model.TeamCardDto>().toDomain()

            // Дополнительный запрос: количество команд в потоке
            val streamId = team.stream?.id
            if (streamId != null) {
                val count = fetchTeamsCount(streamId)
                if (count != null) {
                    team = team.copy(teamsCount = count)
                }
            }

            Result.success(team)
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getTeamById: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getTrackerFullName(username: String): Result<String> {
        return try {
            val role = userInfoHolder.userInfo?.mainRole ?: Role.TRACKER
            println("[TEAM_CARD_REPO] getTrackerFullName: start, username=$username, role=$role")
            val csrf = fetchCsrf()
            println("[TEAM_CARD_REPO] getTrackerFullName: csrf header='${csrf.headerName}', token='${csrf.token.take(10)}...'")

            val response = when (role) {
                // Для всех ролей сначала пробуем получить ФИО по username
                Role.ADMIN, Role.SUPER_ADMIN, Role.TRACKER, Role.UNKNOWN -> client.get("${ApiConstants.USERS_INFO}/$username/info") {
                    header(HttpHeaders.Accept, "application/json")
                    header(csrf.headerName, csrf.token)
                    header("X-Requested-With", "XMLHttpRequest")
                }
            }
            println("[TEAM_CARD_REPO] getTrackerFullName: response status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                println("[TEAM_CARD_REPO] getTrackerFullName: non-OK status, body=${response.bodyAsText()}")
                return Result.failure(Exception("Status: ${response.status}"))
            }

            val dto = response.body<UserInfoDto>()
            println("[TEAM_CARD_REPO] getTrackerFullName: dto.fullName='${dto.fullName}'")
            Result.success(dto.fullName ?: username)
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getTrackerFullName: exception=${e.message}")
            Result.failure(e)
        }
    }

    private suspend fun fetchTeamsCount(streamId: String): Int? {
        return try {
            println("[TEAM_CARD_REPO] fetchTeamsCount: endpoint=${ApiConstants.TEAM_CARD_COUNT}, streamId=$streamId")
            val response = client.get(ApiConstants.TEAM_CARD_COUNT) {
                header(HttpHeaders.Accept, "application/json")
                parameter("streamId", streamId)
            }
            println("[TEAM_CARD_REPO] fetchTeamsCount: status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                println("[TEAM_CARD_REPO] fetchTeamsCount: non-OK status, body=${response.bodyAsText()}")
                return null
            }
            val count = response.body<Int>()
            println("[TEAM_CARD_REPO] fetchTeamsCount: count=$count")
            count
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] fetchTeamsCount: exception=${e.message}")
            null
        }
    }

    private fun com.example.track_me_mobile.features.teams.data.model.TeamCardDto.toDomain() =
        TeamCard(
            id                       = id,
            name                     = name,
            description              = description,
            status                   = status,
            username                 = username,
            enabled                  = enabled,
            ntiMarkets               = ntiMarkets.map { NtiMarket(it.id, it.name, it.displayName) },
            readinessLevel           = readinessLevel,
            averageGrade             = averageGrade,
            stream                   = streams.firstOrNull()
                ?.let { Stream(it.id, it.name, it.description, it.active, it.startDate, it.endDate) },
            meetingsCount            = meetingsCount,
            meetingsCompletedCount   = meetingsCompletedCount,
            meetingsNotHappenedCount = meetingsNotHappenedCount,
            teamsCount               = null
        )
}