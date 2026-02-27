package com.example.track_me_mobile.features.team_card.data

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.team_card.data.models.CreateTeamRequestDto
import com.example.track_me_mobile.features.team_card.data.models.NtiMarketDto
import com.example.track_me_mobile.features.team_card.data.models.TrackerUserDto
import com.example.track_me_mobile.features.team_card.data.models.TrackersPageDto
import com.example.track_me_mobile.features.team_card.domain.TeamCardRepository
import com.example.track_me_mobile.features.team_card.domain.models.CreateTeamRequest
import com.example.track_me_mobile.features.team_card.domain.models.TrackerUser
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

    private val role get() = userInfoHolder.userInfo?.mainRole ?: Role.TRACKER
    private val isAdmin get() = role == Role.ADMIN || role == Role.SUPER_ADMIN
    private val username get() = userInfoHolder.userInfo?.username ?: "unknown"

    private suspend fun fetchCsrf(): CsrfResponse {
        println("[CSRF] fetching token...")
        val csrf = client.get(ApiConstants.CSRF_ENDPOINT) {
            header(HttpHeaders.Accept, "application/json")
        }.body<CsrfResponse>()
        println("[CSRF] got header='${csrf.headerName}', token='${csrf.token.take(8)}...'")
        return csrf
    }

    // ── Просмотр ──────────────────────────────────────────────────────────

    override suspend fun getTeamById(id: String): Result<TeamCard> {
        return try {
            println("[TEAM_CARD_REPO] getTeamById: id=$id")
            val response = client.get(ApiConstants.TEAM_CARD) {
                header(HttpHeaders.Accept, "application/json")
                parameter("id", id)
            }
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Status: ${response.status}"))
            }
            var team = response.body<com.example.track_me_mobile.features.teams.data.model.TeamCardDto>().toDomain()
            val streamId = team.stream?.id
            if (streamId != null) {
                val count = fetchTeamsCount(streamId)
                if (count != null) team = team.copy(teamsCount = count)
            }
            Result.success(team)
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getTeamById error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getTrackerFullName(username: String): Result<String> {
        return try {
            val csrf = fetchCsrf()
            val response = client.get("${ApiConstants.USERS_INFO}/$username/info") {
                header(HttpHeaders.Accept, "application/json")
                header(csrf.headerName, csrf.token)
                header("X-Requested-With", "XMLHttpRequest")
            }
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Status: ${response.status}"))
            }
            val dto = response.body<UserInfoDto>()
            Result.success(dto.fullName ?: username)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Данные для формы создания ─────────────────────────────────────────

    override suspend fun getTrackers(): Result<List<TrackerUser>> {
        return try {
            println("[TEAM_CARD_REPO] getTrackers: start (role=$role, username=$username)")
            val csrf = fetchCsrf()
            val url = ApiConstants.USERS_TRACKERS
            println("[TEAM_CARD_REPO] getTrackers: POST $url")
            val response = client.post(url) {
                header(HttpHeaders.Accept, "application/json")
                header(csrf.headerName, csrf.token)
                header("X-Requested-With", "XMLHttpRequest")
                contentType(ContentType.Application.Json)
                parameter("page", 0)
                parameter("size", 1000)
                setBody("""{"filters":[],"sort":[]}""")
            }
            println("[TEAM_CARD_REPO] getTrackers: status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                val body = response.bodyAsText()
                println("[TEAM_CARD_REPO] getTrackers: error body=$body")
                return Result.failure(Exception("Status: ${response.status} — $body"))
            }
            val page = response.body<TrackersPageDto>()
            println("[TEAM_CARD_REPO] getTrackers: loaded ${page.content.size} trackers")
            Result.success(page.content.map { it.toDomain() })
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getTrackers exception: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getStreams(): Result<List<Stream>> {
        return try {
            println("[TEAM_CARD_REPO] getStreams: start, isAdmin=$isAdmin, role=$role")
            val csrf = fetchCsrf()

            val url = if (isAdmin) ApiConstants.STREAMS_ENDPOINT else ApiConstants.STREAMS_PUBLIC_ENDPOINT
            val body = if (isAdmin) """{"filters":[],"sort":[]}""" else """{"filters":[]}"""
            println("[TEAM_CARD_REPO] getStreams: POST $url")
            println("[TEAM_CARD_REPO] getStreams: body=$body")
            println("[TEAM_CARD_REPO] getStreams: csrf header='${csrf.headerName}'")

            val response = client.post(url) {
                header(HttpHeaders.Accept, "application/json")
                header(csrf.headerName, csrf.token)
                header("X-Requested-With", "XMLHttpRequest")
                contentType(ContentType.Application.Json)
                parameter("page", 0)
                parameter("size", 150)
                setBody(body)
            }

            println("[TEAM_CARD_REPO] getStreams: status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                println("[TEAM_CARD_REPO] getStreams: error body='$errorBody'")
                return Result.failure(Exception("Status: ${response.status} — $errorBody"))
            }

            val page = response.body<com.example.track_me_mobile.features.streams.data.model.StreamPageDto>()
            println("[TEAM_CARD_REPO] getStreams: total=${page.content.size}")

            // Фильтруем только активные — как на веб-клиенте
            val activeStreams = page.content.filter { it.active }
            println("[TEAM_CARD_REPO] getStreams: active=${activeStreams.size}")

            Result.success(activeStreams.map { it.toStream() })
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getStreams exception: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getNtiMarkets(): Result<List<NtiMarket>> {
        return try {
            println("[TEAM_CARD_REPO] getNtiMarkets: GET ${ApiConstants.NTI_MARKETS_ENDPOINT}")
            val response = client.get(ApiConstants.NTI_MARKETS_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }
            println("[TEAM_CARD_REPO] getNtiMarkets: status=${response.status}")
            if (response.status != HttpStatusCode.OK) {
                val body = response.bodyAsText()
                println("[TEAM_CARD_REPO] getNtiMarkets: error body=$body")
                return Result.failure(Exception("Status: ${response.status}"))
            }
            val dtos = response.body<List<NtiMarketDto>>()
            println("[TEAM_CARD_REPO] getNtiMarkets: loaded ${dtos.size} markets")
            Result.success(dtos.map { it.toDomain() })
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] getNtiMarkets exception: ${e.message}")
            Result.failure(e)
        }
    }

    // ── Создание ──────────────────────────────────────────────────────────

    override suspend fun createTeam(request: CreateTeamRequest): Result<Unit> {
        return try {
            println("[TEAM_CARD_REPO] createTeam: start")
            println("[TEAM_CARD_REPO] createTeam: name='${request.name}', tracker='${request.trackerUsername}'")
            println("[TEAM_CARD_REPO] createTeam: streamId='${request.streamId}', trl='${request.readinessLevel}'")
            println("[TEAM_CARD_REPO] createTeam: markets=${request.ntiMarketIds}")
            println("[TEAM_CARD_REPO] createTeam: isAdmin=$isAdmin, role=$role")

            val csrf = fetchCsrf()
            // /backend/api/v1/team-card — для обеих ролей (трекер и админ)
            val url = ApiConstants.TEAM_CARD
            println("[TEAM_CARD_REPO] createTeam: POST $url?streamId=${request.streamId}")

            val response = client.post(url) {
                header(HttpHeaders.Accept, "application/json")
                header(csrf.headerName, csrf.token)
                header("X-Requested-With", "XMLHttpRequest")
                contentType(ContentType.Application.Json)
                parameter("streamId", request.streamId)
                setBody(
                    CreateTeamRequestDto(
                        name            = request.name,
                        description     = request.description,
                        trackerUsername = request.trackerUsername,
                        streamId        = request.streamId,
                        ntiMarketIds    = request.ntiMarketIds,
                        readinessLevel  = request.readinessLevel
                    )
                )
            }
            println("[TEAM_CARD_REPO] createTeam: status=${response.status}")
            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.Created) {
                val body = response.bodyAsText()
                println("[TEAM_CARD_REPO] createTeam: error body=$body")
                return Result.failure(Exception("Status: ${response.status}\n$body"))
            }
            println("[TEAM_CARD_REPO] createTeam: SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            println("[TEAM_CARD_REPO] createTeam exception: ${e.message}")
            Result.failure(e)
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private suspend fun fetchTeamsCount(streamId: String): Int? {
        return try {
            val response = client.get(ApiConstants.TEAM_CARD_COUNT) {
                header(HttpHeaders.Accept, "application/json")
                parameter("streamId", streamId)
            }
            if (response.status != HttpStatusCode.OK) return null
            response.body<Int>()
        } catch (e: Exception) {
            null
        }
    }

    private fun com.example.track_me_mobile.features.streams.data.model.StreamDto.toStream() =
        Stream(
            id          = id,
            name        = name,
            description = description ?: "",
            active      = active,
            startDate   = startDate,
            endDate     = endDate
        )

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