package com.example.track_me_mobile.features.teams.data

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.network.ApiConstants
import com.example.track_me_mobile.features.teams.data.model.TeamCardsPageDto
import com.example.track_me_mobile.features.teams.domain.TeamRepository
import com.example.track_me_mobile.features.teams.domain.models.NtiMarket
import com.example.track_me_mobile.features.teams.domain.models.Stream
import com.example.track_me_mobile.features.teams.domain.models.TeamCard
import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TeamFilterDto(
    val fieldName: String,
    val type: String,
    val values: List<String>
)

@Serializable
data class TeamSortDto(
    val field: String,
    val direction: String
)

@Serializable
data class TeamRequestBody(
    val filters: List<TeamFilterDto>,
    val sort: List<TeamSortDto>
)

class TeamRepositoryImpl(
    private val client: HttpClient,
    private val userInfoHolder: UserInfoHolder
) : TeamRepository {

    override suspend fun getTeamCards(
        streamId: String?,
        page: Int,
        size: Int
    ): Result<List<TeamCard>> {
        return try {
            val userInfo = userInfoHolder.userInfo
            val role = userInfo?.mainRole ?: Role.TRACKER

            // Шаг 1: получаем CSRF-токен
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            val csrfData = csrfResponse.body<CsrfResponse>()

            // Шаг 2: формируем фильтры
            val filters = mutableListOf<TeamFilterDto>()

            // Для TRACKER добавляем фильтр по username
            if (role == Role.TRACKER && userInfo != null) {
                filters.add(TeamFilterDto(
                    fieldName = "username",
                    type = "EQ",
                    values = listOf(userInfo.username)
                ))
            }

            // Фильтр по потоку (если передан)
            if (streamId != null) {
                filters.add(TeamFilterDto(
                    fieldName = "streams.name",
                    type = "EQ",
                    values = listOf(streamId)
                ))
            }

            // Шаг 3: сортировка
            val sort = listOf(
                TeamSortDto(field = "enabled", direction = "desc"),
                TeamSortDto(field = "streams.name", direction = "desc"),
                TeamSortDto(field = "averageGrade", direction = "desc")
            )

            val requestBody = TeamRequestBody(filters = filters, sort = sort)

            // Шаг 4: запрос к backend
            val endpoint = when (role) {
                Role.ADMIN, Role.SUPER_ADMIN ->
                    "${ApiConstants.BACKEND_BASE}/api/v1/admin/team-cards?page=$page&size=$size"
                else ->
                    "${ApiConstants.BACKEND_BASE}/api/v1/team-cards?page=$page&size=$size"
            }

            println("[TEAMS] Role: $role, Username: ${userInfo?.username}")
            println("[TEAMS] Request to: $endpoint")
            println("[TEAMS] Body: ${Json.encodeToString(TeamRequestBody.serializer(), requestBody)}")

            val response = client.post(endpoint) {
                header(HttpHeaders.Accept, "application/json")
                header(csrfData.headerName, csrfData.token)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            println("[TEAMS] Status: ${response.status}")
            val bodyText = response.bodyAsText()
            println("[TEAMS] Body (first 300): ${bodyText.take(300)}")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Status: ${response.status}"))
            }

            val dto = response.body<TeamCardsPageDto>()
            Result.success(dto.content.map { it.toDomain() })

        } catch (e: Exception) {
            println("[TEAMS] EXCEPTION: ${e::class.simpleName} – ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun com.example.track_me_mobile.features.teams.data.model.TeamCardDto.toDomain() = TeamCard(
        id                       = id,
        name                     = name,
        description              = description,
        status                   = status,
        username                 = username,
        enabled                  = enabled,
        ntiMarkets               = ntiMarkets.map { NtiMarket(it.id, it.name, it.displayName) },
        readinessLevel           = readinessLevel,
        averageGrade             = averageGrade,
        stream                   = streams.firstOrNull()?.let { Stream(it.id, it.name, it.description, it.active) },
        meetingsCount            = meetingsCount,
        meetingsCompletedCount   = meetingsCompletedCount,
        meetingsNotHappenedCount = meetingsNotHappenedCount
    )
}