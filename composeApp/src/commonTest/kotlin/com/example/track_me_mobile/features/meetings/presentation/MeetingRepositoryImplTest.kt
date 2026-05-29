package com.example.track_me_mobile.features.meetings.data

import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.meetings.domain.models.MeetingUpdateRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class MeetingRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }



    @Test
    fun `getMeetings returns failure on HTTP error`() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }

        val client = HttpClient(mockEngine)
        val repository = MeetingRepositoryImpl(client)

        val result = repository.getMeetings(teamCardId = "team-123", page = 0, size = 10)

        assertTrue(result.isFailure)
    }



    @Test
    fun `updateMeeting returns failure on HTTP error`() = runTest {
        var requestCount = 0

        val mockEngine = MockEngine { request ->
            requestCount++

            when {
                requestCount == 1 -> {
                    val csrfResponse = CsrfResponse(
                        token = "token",
                        headerName = "X-CSRF-TOKEN",
                        parameterName = "_csrf"
                    )
                    respond(
                        content = json.encodeToString(CsrfResponse.serializer(), csrfResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json"))
                    )
                }
                requestCount == 2 -> {
                    respond(content = "Bad Request", status = HttpStatusCode.BadRequest)
                }
                else -> {
                    respond(content = "", status = HttpStatusCode.NotFound)
                }
            }
        }

        val client = HttpClient(mockEngine)
        val repository = MeetingRepositoryImpl(client)

        val updateRequest = MeetingUpdateRequest(
            id = "meeting-1",
            link = "https://link.com",
            number = "001",
            teamStatus = "OK",
            tasksCurrentMeeting = "",
            tasksNextMeeting = "",
            startDate = "2024-01-01",
            status = "SCHEDULED",
            teamCardId = "team-123"
        )

        val result = repository.updateMeeting("meeting-1", "team-123", updateRequest)

        assertTrue(result.isFailure)
    }



    @Test
    fun `deleteMeeting returns failure on HTTP error`() = runTest {
        var requestCount = 0

        val mockEngine = MockEngine { request ->
            requestCount++

            when {
                requestCount == 1 -> {
                    val csrfResponse = CsrfResponse(
                        token = "token",
                        headerName = "X-CSRF-TOKEN",
                        parameterName = "_csrf"
                    )
                    respond(
                        content = json.encodeToString(CsrfResponse.serializer(), csrfResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json"))
                    )
                }
                requestCount == 2 -> {
                    respond(content = "Not Found", status = HttpStatusCode.NotFound)
                }
                else -> {
                    respond(content = "", status = HttpStatusCode.NotFound)
                }
            }
        }

        val client = HttpClient(mockEngine)
        val repository = MeetingRepositoryImpl(client)

        val result = repository.deleteMeeting("invalid-id")

        assertTrue(result.isFailure)
    }


    @Test
    fun `uploadImage handles server error`() = runTest {
        var requestCount = 0

        val mockEngine = MockEngine { request ->
            requestCount++

            when {
                requestCount == 1 -> {
                    val csrfResponse = CsrfResponse(
                        token = "token",
                        headerName = "X-CSRF-TOKEN",
                        parameterName = "_csrf"
                    )
                    respond(
                        content = json.encodeToString(CsrfResponse.serializer(), csrfResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json"))
                    )
                }
                requestCount == 2 -> {
                    respond(content = "Payload Too Large", status = HttpStatusCode.PayloadTooLarge)
                }
                else -> {
                    respond(content = "", status = HttpStatusCode.NotFound)
                }
            }
        }

        val client = HttpClient(mockEngine)
        val repository = MeetingRepositoryImpl(client)

        val result = repository.uploadImage("meeting-1", byteArrayOf(1, 2, 3))

        assertTrue(result.isFailure)
    }



    @Test
    fun `createMeeting returns failure on HTTP error`() = runTest {
        var requestCount = 0

        val mockEngine = MockEngine { request ->
            requestCount++

            when {
                requestCount == 1 -> {
                    val csrfResponse = CsrfResponse(
                        token = "token",
                        headerName = "X-CSRF-TOKEN",
                        parameterName = "_csrf"
                    )
                    respond(
                        content = json.encodeToString(CsrfResponse.serializer(), csrfResponse),
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type" to listOf("application/json"))
                    )
                }
                requestCount == 2 -> {
                    respond(content = "Bad Request", status = HttpStatusCode.BadRequest)
                }
                else -> {
                    respond(content = "", status = HttpStatusCode.NotFound)
                }
            }
        }

        val client = HttpClient(mockEngine)
        val repository = MeetingRepositoryImpl(client)

        val result = repository.createMeeting(
            teamCardId = "invalid-team",
            startDateIso = "2024-02-01T10:00:00",
            number = "003"
        )

        assertTrue(result.isFailure)
    }
}