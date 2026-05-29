package com.example.track_me_mobile.features.streams.data

import com.example.track_me_mobile.features.streams.data.model.NtiMarketDto
import com.example.track_me_mobile.features.streams.data.model.PageMetaDto
import com.example.track_me_mobile.features.streams.data.model.StreamCreateDto
import com.example.track_me_mobile.features.streams.data.model.StreamDto
import com.example.track_me_mobile.features.streams.data.model.StreamFilterDto
import com.example.track_me_mobile.features.streams.data.model.StreamFilterRequest
import com.example.track_me_mobile.features.streams.data.model.StreamPageDto
import com.example.track_me_mobile.features.streams.domain.models.NtiMarket
import com.example.track_me_mobile.features.streams.domain.models.StreamCreateRequest
import com.example.track_me_mobile.features.streams.domain.models.StreamFilter
import com.example.track_me_mobile.features.teams.data.model.NtiMarketDto as TeamNtiMarketDto
import com.example.track_me_mobile.features.teams.data.model.StreamDto as TeamStreamDto
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

class StreamsDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `StreamDto and StreamPageDto serialize`() {
        val page = StreamPageDto(
            content = listOf(
                StreamDto(
                    id = "s1",
                    name = "Stream 1",
                    startDate = "2024-01-01",
                    endDate = "2024-12-31",
                    description = "Desc",
                    active = true,
                    trackStartDate = "2024-02-01",
                    meetingsCount = 10,
                    ntiMarkets = listOf(NtiMarketDto("m1", "market", "Market"))
                )
            ),
            page = PageMetaDto(size = 1, number = 0, totalElements = 1, totalPages = 1)
        )
        val encoded = json.encodeToString(StreamPageDto.serializer(), page)
        val decoded = json.decodeFromString(StreamPageDto.serializer(), encoded)
        assertEquals("Stream 1", decoded.content.first().name)
    }

    @Test
    fun `StreamFilterRequest serializes filters`() {
        val request = StreamFilterRequest(
            filters = listOf(StreamFilterDto("year", "EQ", listOf("2024")))
        )
        val encoded = json.encodeToString(StreamFilterRequest.serializer(), request)
        assertTrue(encoded.contains("year"))
    }

    @Test
    fun `StreamCreateDto serializes all fields`() {
        val dto = StreamCreateDto(
            name = "New",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            ntiMarketIds = listOf("m1"),
            description = "Desc",
            trackStartDate = "2024-02-01",
            meetingsCount = 5
        )
        val encoded = json.encodeToString(StreamCreateDto.serializer(), dto)
        assertTrue(encoded.contains("meetingsCount"))
    }

    @Test
    fun `StreamRepositoryImpl getStreams success`() = runTest {
        var requestCount = 0
        val streamsJson = """
            {
              "content": [{
                "id": "s1",
                "name": "Stream A",
                "startDate": "2024-01-01",
                "endDate": "2024-12-31",
                "description": "D",
                "active": true,
                "trackStartDate": "2024-02-01",
                "meetingsCount": 5,
                "ntiMarkets": [{"id":"m1","name":"m","displayName":"M"}]
              }],
              "page": {"size":1,"number":0,"totalElements":1,"totalPages":1}
            }
        """.trimIndent()

        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = streamsJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.getStreams(
            filters = listOf(StreamFilter("year", "EQ", "2024")),
            page = 0,
            size = 10
        )

        assertTrue(result.isSuccess)
        assertEquals("Stream A", result.getOrNull()?.content?.first()?.name)
    }

    @Test
    fun `StreamRepositoryImpl getNtiMarkets success`() = runTest {
        val marketsJson = """[{"id":"m1","name":"market","displayName":"Market 1"}]"""
        val engine = MockEngine {
            respond(content = marketsJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.getNtiMarkets()
        assertTrue(result.isSuccess)
        assertEquals(NtiMarket("m1", "market", "Market 1"), result.getOrNull()?.first())
    }

    @Test
    fun `StreamRepositoryImpl getStream success`() = runTest {
        val streamJson = """
            {
              "id": "s1",
              "name": "Stream",
              "startDate": "2024-01-01",
              "endDate": "2024-12-31",
              "description": "D",
              "active": true,
              "trackStartDate": "2024-02-01",
              "meetingsCount": 3,
              "ntiMarkets": []
            }
        """.trimIndent()
        val engine = MockEngine {
            respond(content = streamJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getStream("s1").isSuccess)
    }

    @Test
    fun `StreamRepositoryImpl createStream success`() = runTest {
        var requestCount = 0
        val streamJson = """
            {
              "id": "new-id",
              "name": "Created",
              "startDate": "2024-01-01",
              "endDate": "2024-12-31",
              "description": "D",
              "active": true,
              "trackStartDate": "2024-02-01",
              "meetingsCount": 5,
              "ntiMarkets": []
            }
        """.trimIndent()
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = streamJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))
        val request = StreamCreateRequest(
            name = "Created",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            ntiMarketIds = emptyList(),
            description = "D",
            trackStartDate = "2024-02-01",
            meetingsCount = 5
        )

        assertTrue(repository.createStream(request).isSuccess)
    }

    @Test
    fun `StreamRepositoryImpl deleteStream returns StreamHasTeamsException on foreign key error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "foreign key constraint fk_streams_team_cards_stream", status = HttpStatusCode.BadRequest)
            }
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.deleteStream("s1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is StreamHasTeamsException)
    }

    @Test
    fun `StreamRepositoryImpl deleteStream success on OK`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "", status = HttpStatusCode.NoContent)
            }
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.deleteStream("s1").isSuccess)
    }

    @Test
    fun `StreamRepositoryImpl getStreamImage returns null on 404`() = runTest {
        val engine = MockEngine {
            respond(content = "", status = HttpStatusCode.NotFound)
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.getStreamImage("s1")
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun `StreamRepositoryImpl getStreamImage returns bytes on OK`() = runTest {
        val engine = MockEngine {
            respond(content = byteArrayOf(9, 8, 7), status = HttpStatusCode.OK)
        }
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.getStreamImage("s1")
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `StreamRepositoryImpl getTeamsByStream filters teams by stream id`() = runTest {
        var requestCount = 0
        val teamsJson = json.encodeToString(
            TeamCardsPageDto.serializer(),
            TeamCardsPageDto(
                content = listOf(
                    TeamCardDto(
                        id = "t1",
                        name = "Team In Stream",
                        meetingRoomLink = "link",
                        description = "d",
                        status = "ACTIVE",
                        username = "u",
                        enabled = true,
                        streams = listOf(TeamStreamDto("s1", "Stream", null, null, "d", true))
                    ),
                    TeamCardDto(
                        id = "t2",
                        name = "Other Team",
                        meetingRoomLink = "link",
                        description = "d",
                        status = "ACTIVE",
                        username = "u2",
                        enabled = true,
                        streams = listOf(TeamStreamDto("s2", "Other", null, null, "d", true))
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
        val repository = StreamRepositoryImpl(testHttpClient(engine))

        val result = repository.getTeamsByStream("s1")
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Team In Stream", result.getOrNull()?.first()?.name)
    }

    @Test
    fun `StreamHasTeamsException carries message`() {
        val ex = StreamHasTeamsException("teams exist")
        assertEquals("teams exist", ex.message)
    }
}
