package com.example.track_me_mobile.features.reports.data

import com.example.track_me_mobile.features.reports.data.model.PageInfo
import com.example.track_me_mobile.features.reports.data.model.ReportItemDto
import com.example.track_me_mobile.features.reports.data.model.ReportsPageDto
import com.example.track_me_mobile.features.reports.domain.models.ReportItem
import com.example.track_me_mobile.features.reports.domain.models.StreamMeetingReportItem
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

class ReportsDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `ReportItemDto toDomain maps nullable grades to zero`() {
        val dto = ReportItemDto(
            streamId = "s1",
            streamName = "Stream A",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            teamId = "t1",
            teamCardName = "Team Alpha",
            username = "tracker1",
            averageTeamGrade = null,
            averageUserGrade = null,
            meetingsCountPlan = 10,
            meetingsCountFact = 8,
            ntiMarkets = listOf("Market1"),
            readinessLevel = "5-7"
        )
        val domain = dto.toDomain()
        assertEquals(0.0, domain.averageTeamGrade)
        assertEquals(0.0, domain.averageUserGrade)
        assertEquals("Team Alpha", domain.teamCardName)
    }

    @Test
    fun `ReportItemDto toDomain preserves non-null grades`() {
        val dto = ReportItemDto(
            streamName = "Stream B",
            startDate = "2024-02-01",
            endDate = "2024-11-30",
            teamCardName = "Team Beta",
            username = "tracker2",
            averageTeamGrade = 4.5,
            averageUserGrade = 3.8,
            meetingsCountPlan = 5,
            meetingsCountFact = 5,
            ntiMarkets = emptyList(),
            readinessLevel = "3-5"
        )
        val domain = dto.toDomain()
        assertEquals(4.5, domain.averageTeamGrade)
        assertEquals(3.8, domain.averageUserGrade)
    }

    @Test
    fun `ReportsPageDto serializes and deserializes`() {
        val page = ReportsPageDto(
            content = listOf(
                ReportItemDto(
                    streamName = "S",
                    startDate = "2024-01-01",
                    endDate = "2024-12-31",
                    teamCardName = "T",
                    username = "u",
                    averageTeamGrade = 1.0,
                    averageUserGrade = 2.0,
                    meetingsCountPlan = 1,
                    meetingsCountFact = 1,
                    ntiMarkets = listOf("M"),
                    readinessLevel = "1"
                )
            ),
            page = PageInfo(size = 1, number = 0, totalElements = 1, totalPages = 1)
        )
        val encoded = json.encodeToString(ReportsPageDto.serializer(), page)
        val decoded = json.decodeFromString(ReportsPageDto.serializer(), encoded)
        assertEquals(page.content.size, decoded.content.size)
        assertEquals(page.page.totalPages, decoded.page.totalPages)
    }

    @Test
    fun `ReportFilterDto and ReportRequestBody serialize`() {
        val body = ReportRequestBody(
            filters = listOf(
                ReportFilterDto(fieldName = "username", type = "EQ", values = listOf("alice"))
            )
        )
        val encoded = json.encodeToString(ReportRequestBody.serializer(), body)
        assertTrue(encoded.contains("username"))
        assertTrue(encoded.contains("alice"))
    }

    @Test
    fun `ReportRepositoryImpl getReports success returns mapped items`() = runTest {
        var requestCount = 0
        val reportsJson = """
            {
              "content": [{
                "streamName": "Stream 1",
                "startDate": "2024-01-01",
                "endDate": "2024-12-31",
                "teamCardName": "Team 1",
                "username": "tracker",
                "averageTeamGrade": 4.0,
                "averageUserGrade": 3.0,
                "meetingsCountPlan": 10,
                "meetingsCountFact": 9,
                "ntiMarkets": ["M1"],
                "readinessLevel": "5"
              }],
              "page": {"size": 1, "number": 0, "totalElements": 1, "totalPages": 1}
            }
        """.trimIndent()

        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(
                    content = csrfJson(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
                else -> respond(
                    content = reportsJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
            }
        }
        val repository = ReportRepositoryImpl(testHttpClient(engine))

        val result = repository.getReports(
            trackerUsername = "tracker",
            streamName = "Stream 1",
            showInactive = true
        )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("Team 1", result.getOrNull()?.first()?.teamCardName)
    }

    @Test
    fun `ReportRepositoryImpl getReports returns failure on HTTP error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.InternalServerError)
            }
        }
        val repository = ReportRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getReports(null, null).isFailure)
    }

    @Test
    fun `ReportRepositoryImpl downloadReportsExcel returns bytes on success`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = byteArrayOf(1, 2, 3), status = HttpStatusCode.OK)
            }
        }
        val repository = ReportRepositoryImpl(testHttpClient(engine))

        val result = repository.downloadReportsExcel(null, null, showInactive = false)

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()?.size)
    }

    @Test
    fun `StreamMeetingReportItemDto toDomain maps all fields`() {
        val dto = StreamMeetingReportItemDto(
            streamId = "s1",
            streamName = "Stream",
            teamId = "t1",
            teamName = "Team",
            startDate = "2024-05-01",
            trackerName = "tr",
            trackerFullName = "Tracker Full",
            tasksNextMeeting = "next",
            tasksCurrentMeeting = "current",
            status = "COMPLETED",
            teamStatus = "OK"
        )
        val domain = dto.toDomain()
        assertEquals(StreamMeetingReportItem(
            streamId = "s1",
            streamName = "Stream",
            teamId = "t1",
            teamName = "Team",
            startDate = "2024-05-01",
            trackerName = "tr",
            trackerFullName = "Tracker Full",
            tasksNextMeeting = "next",
            tasksCurrentMeeting = "current",
            status = "COMPLETED",
            teamStatus = "OK"
        ), domain)
    }

    @Test
    fun `StreamMeetingReportRepositoryImpl getReportsByStream success`() = runTest {
        var requestCount = 0
        val responseJson = """
            {
              "content": [{
                "teamName": "Alpha",
                "trackerFullName": "Alice",
                "teamStatus": "OK"
              }]
            }
        """.trimIndent()

        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = StreamMeetingReportRepositoryImpl(testHttpClient(engine))

        val result = repository.getReportsByStream("stream-1", page = 0, size = 100, sort = listOf("teamName,asc"))

        assertTrue(result.isSuccess)
        assertEquals("Alpha", result.getOrNull()?.first()?.teamName)
    }

    @Test
    fun `StreamMeetingReportRepositoryImpl getReportsByStream failure on bad status`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.BadRequest)
            }
        }
        val repository = StreamMeetingReportRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getReportsByStream("s1", 0, 10, emptyList()).isFailure)
    }

    @Test
    fun `StreamMeetingReportRepositoryImpl downloadReportsExcel with filters success`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = byteArrayOf(10, 20), status = HttpStatusCode.OK)
            }
        }
        val repository = StreamMeetingReportRepositoryImpl(testHttpClient(engine))

        val result = repository.downloadReportsExcel(
            streamId = "stream-1",
            trackerFilter = "Alice",
            teamFilter = "Alpha",
            statusFilter = "OK",
            page = 0,
            size = 100,
            sort = listOf("teamName,asc")
        )

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun `ReportItem domain model copy and equality`() {
        val item = ReportItem(
            streamName = "S",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            teamCardName = "T",
            username = "u",
            averageTeamGrade = 1.0,
            averageUserGrade = 2.0,
            meetingsCountPlan = 1,
            meetingsCountFact = 1,
            ntiMarkets = listOf("M"),
            readinessLevel = "5"
        )
        val copy = item.copy(streamName = "S2")
        assertEquals("S2", copy.streamName)
        assertEquals(item.username, copy.username)
    }
}
