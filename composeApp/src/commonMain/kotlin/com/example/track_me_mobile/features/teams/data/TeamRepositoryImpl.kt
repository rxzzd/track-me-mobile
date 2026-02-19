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

class TeamRepositoryImpl(
    private val client: HttpClient,
    private val userInfoHolder: UserInfoHolder  // ← читаем роль из холдера
) : TeamRepository {

    override suspend fun getTeamCards(page: Int, size: Int): Result<List<TeamCard>> {
        return try {

            val role = userInfoHolder.userInfo?.mainRole ?: Role.TRACKER


            // Шаг 1: получаем CSRF-токен — он нужен как Bearer для backend
            val csrfResponse = client.get(ApiConstants.CSRF_ENDPOINT) {
                header(HttpHeaders.Accept, "application/json")
            }

            val csrfData = csrfResponse.body<CsrfResponse>()

            // Шаг 2: запрос к backend с токеном как Bearer
            val endpoint = when (role) {
                Role.ADMIN, Role.SUPER_ADMIN ->
                    "${ApiConstants.BACKEND_BASE}/api/v1/admin/team-cards?page=$page&size=$size"
                else ->
                    "${ApiConstants.BACKEND_BASE}/api/v1/team-cards?page=$page&size=$size"
            }

            val response = client.post(endpoint) {
                header(HttpHeaders.Accept,       "application/json")
                header(csrfData.headerName,      csrfData.token)
                contentType(ContentType.Application.Json)
                setBody("""{"filters": []}""")  // ← пустое тело, сервер требует его наличие
            }

            println("[TEAMS] Статус: ${response.status}")
            val bodyText = response.bodyAsText()
            println("[TEAMS] Тело (первые 300): ${bodyText.take(300)}")

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Статус: ${response.status}"))
            }

            val dto = response.body<TeamCardsPageDto>()
            Result.success(dto.content.map { it.toDomain() })

        } catch (e: Exception) {
            println("[TEAMS] ИСКЛЮЧЕНИЕ: ${e::class.simpleName} — ${e.message}")
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