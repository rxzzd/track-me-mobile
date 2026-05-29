package com.example.track_me_mobile.features.team_card.data

import com.example.track_me_mobile.core.domain.UserInfoHolder
import com.example.track_me_mobile.core.domain.models.Role
import com.example.track_me_mobile.core.domain.models.UserInfo
import com.example.track_me_mobile.features.team_card.data.models.CreateTeamRequestDto
import com.example.track_me_mobile.features.team_card.data.models.NtiMarketDto
import com.example.track_me_mobile.features.team_card.data.models.TrackerUserDto
import com.example.track_me_mobile.features.team_card.data.models.TrackersPageDto
import com.example.track_me_mobile.features.team_card.data.models.UpdateTeamRequestDto
import com.example.track_me_mobile.features.teams.data.model.TeamCardDto
import com.example.track_me_mobile.features.teams.data.model.StreamDto
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

class TeamCardDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `NtiMarketDto toDomain uses name when displayName is null`() {
        val dto = NtiMarketDto(id = "m1", name = "market", displayName = null)
        val domain = dto.toDomain()
        assertEquals("market", domain.displayName)
    }

    @Test
    fun `TrackerUserDto toDomain falls back fullName to username`() {
        val dto = TrackerUserDto(id = "1", username = "nick", fullName = null)
        assertEquals("nick", dto.toDomain().fullName)
    }

    @Test
    fun `CreateTeamRequestDto and UpdateTeamRequestDto serialize`() {
        val create = CreateTeamRequestDto("Team", "https://link.com", "desc", listOf("m1"), "5-7")
        val update = UpdateTeamRequestDto("Team", "https://link.com", "desc", listOf("m1"), "5-7")
        assertTrue(json.encodeToString(CreateTeamRequestDto.serializer(), create).contains("Team"))
        assertTrue(json.encodeToString(UpdateTeamRequestDto.serializer(), update).contains("5-7"))
    }

    @Test
    fun `TrackersPageDto deserializes content list`() {
        val page = TrackersPageDto(
            content = listOf(TrackerUserDto("1", "alice", "Alice"))
        )
        val encoded = json.encodeToString(TrackersPageDto.serializer(), page)
        val decoded = json.decodeFromString(TrackersPageDto.serializer(), encoded)
        assertEquals(1, decoded.content.size)
    }

    @Test
    fun `getTeamById returns team on success`() = runTest {
        val teamJson = json.encodeToString(
            TeamCardDto.serializer(),
            TeamCardDto(
                id = "team-1",
                name = "Alpha",
                meetingRoomLink = "https://link.com",
                description = "Description long enough",
                status = "ACTIVE",
                username = "tracker",
                enabled = true,
                streams = listOf(StreamDto("s1", "Stream", null, null, "d", true))
            )
        )
        var requestCount = 0
        val engine = MockEngine { httpRequest ->
            requestCount++
            when {
                httpRequest.url.encodedPath.contains("count") -> respond(content = "5", status = HttpStatusCode.OK)
                else -> respond(content = teamJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = TeamCardRepositoryImpl(
            testHttpClient(engine),
            adminUserInfoHolder()
        )

        val result = repository.getTeamById("team-1")
        assertTrue(result.isSuccess)
        assertEquals("Alpha", result.getOrNull()?.name)
    }

    @Test
    fun `getTeamById returns failure on HTTP error`() = runTest {
        val engine = MockEngine {
            respond(content = "err", status = HttpStatusCode.NotFound)
        }
        val repository = TeamCardRepositoryImpl(testHttpClient(engine), adminUserInfoHolder())

        assertTrue(repository.getTeamById("missing").isFailure)
    }

    @Test
    fun `getTrackerFullName for admin uses users info endpoint`() = runTest {
        val userJson = """{"id":"1","username":"tracker","roles":["TRACKER"],"fullName":"Tracker Name","email":"t@e.com","enabled":true}"""
        val engine = MockEngine {
            respond(content = userJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
        }
        val repository = TeamCardRepositoryImpl(testHttpClient(engine), adminUserInfoHolder())

        val result = repository.getTrackerFullName("tracker")
        assertTrue(result.isSuccess)
        assertEquals("Tracker Name", result.getOrNull())
    }

    @Test
    fun `getTrackers returns list on success`() = runTest {
        var requestCount = 0
        val trackersJson = json.encodeToString(
            TrackersPageDto.serializer(),
            TrackersPageDto(content = listOf(TrackerUserDto("1", "alice", "Alice")))
        )
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = trackersJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = TeamCardRepositoryImpl(testHttpClient(engine), adminUserInfoHolder())

        val result = repository.getTrackers()
        assertTrue(result.isSuccess)
        assertEquals("alice", result.getOrNull()?.first()?.username)
    }

    @Test
    fun `getTrackers returns failure on HTTP error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.BadRequest)
            }
        }
        val repository = TeamCardRepositoryImpl(testHttpClient(engine), adminUserInfoHolder())

        assertTrue(repository.getTrackers().isFailure)
    }

    private fun adminUserInfoHolder(): UserInfoHolder {
        val holder = UserInfoHolder()
        holder.save(UserInfo("1", "admin", "Admin", "a@a.com", listOf(Role.ADMIN)))
        return holder
    }
}
