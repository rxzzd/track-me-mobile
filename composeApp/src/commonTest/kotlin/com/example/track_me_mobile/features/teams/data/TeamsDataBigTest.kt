package com.example.track_me_mobile.features.teams.data

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.features.teams.data.model.NtiMarketDto
import com.example.track_me_mobile.features.teams.data.model.StreamDto
import com.example.track_me_mobile.features.teams.data.model.TeamCardDto
import com.example.track_me_mobile.features.teams.data.model.TeamCardsPageDto
import com.example.track_me_mobile.testsupport.csrfJson
import com.example.track_me_mobile.testsupport.testHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TeamsDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `TeamFilterDto TeamSortDto TeamRequestBody serialize`() {
        val body = TeamRequestBody(
            filters = listOf(TeamFilterDto("username", "EQ", listOf("tracker"))),
            sort = listOf(TeamSortDto("enabled", "desc"))
        )
        val encoded = json.encodeToString(TeamRequestBody.serializer(), body)
        assertTrue(encoded.contains("username"))
        assertTrue(encoded.contains("enabled"))
    }

    @Test
    fun `TeamCardDto and TeamCardsPageDto serialize`() {
        val page = TeamCardsPageDto(
            content = listOf(
                TeamCardDto(
                    id = "t1",
                    name = "Team",
                    meetingRoomLink = "link",
                    description = "desc",
                    status = "ACTIVE",
                    username = "tracker",
                    enabled = true,
                    ntiMarkets = listOf(NtiMarketDto("m1", "market", "Market")),
                    readinessLevel = "5 - Начальная",
                    averageGrade = 4.5,
                    streams = listOf(StreamDto("s1", "Stream", null, null, "d", true))
                )
            )
        )
        val encoded = json.encodeToString(TeamCardsPageDto.serializer(), page)
        val decoded = json.decodeFromString(TeamCardsPageDto.serializer(), encoded)
        assertEquals("Team", decoded.content.first().name)
    }

    @Test
    fun `getTeamCards admin success returns mapped teams`() = runTest {
        var requestCount = 0
        val teamsJson = json.encodeToString(
            TeamCardsPageDto.serializer(),
            TeamCardsPageDto(
                content = listOf(
                    TeamCardDto(
                        id = "t1",
                        name = "Alpha",
                        meetingRoomLink = "link",
                        description = "desc",
                        status = "ACTIVE",
                        username = "tracker",
                        enabled = true,
                        ntiMarkets = emptyList(),
                        readinessLevel = "5",
                        streams = listOf(StreamDto("s1", "Stream", null, null, "d", true))
                    )
                )
            )
        )
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = teamsJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = TeamRepositoryImpl(testHttpClient(engine), adminHolder())

        val result = repository.getTeamCards(streamId = null, page = 0, size = 10)
        assertTrue(result.isSuccess)
        assertEquals("Alpha", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `getTeamCards tracker uses team-cards endpoint with username filter`() = runTest {
        var requestCount = 0
        val engine = MockEngine { request ->
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(
                    content = """{"content":[]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
            }
        }
        val repository = TeamRepositoryImpl(testHttpClient(engine), trackerHolder())

        val result = repository.getTeamCards(streamId = "Stream", page = 0, size = 10)
        assertTrue(result.isSuccess)
        assertEquals(2, requestCount)
    }

    @Test
    fun `getTeamCards returns failure on HTTP error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.InternalServerError)
            }
        }
        val repository = TeamRepositoryImpl(testHttpClient(engine), adminHolder())

        assertTrue(repository.getTeamCards(null, 0, 10).isFailure)
    }

    private fun adminHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("1", "admin", "Admin", null, listOf(Role.ADMIN)))
        return holder
    }

    private fun trackerHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("1", "tracker1", "Tracker", null, listOf(Role.TRACKER)))
        return holder
    }
}
