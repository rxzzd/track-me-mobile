package com.example.track_me_mobile.features.profile.data

import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.profile.domain.models.UserProfile
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProfileDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `AccountInfoDto serializes and deserializes all fields`() {
        val dto = AccountInfoDto(
            id = "user-1",
            username = "john",
            roles = listOf("TRACKER"),
            fullName = "John Doe",
            email = "john@example.com",
            phoneNumber = "+70000000001",
            avatarUrl = "https://avatar.test/john.png",
            enabled = true
        )
        val encoded = json.encodeToString(AccountInfoDto.serializer(), dto)
        val decoded = json.decodeFromString(AccountInfoDto.serializer(), encoded)
        assertEquals(dto, decoded)
    }

    @Test
    fun `UpdateAccountRequest serializes nullable avatarUrl`() {
        val withAvatar = UpdateAccountRequest("Name", "a@b.com", "+7", "url")
        val withoutAvatar = UpdateAccountRequest("Name", "a@b.com", "+7", null)
        assertTrue(json.encodeToString(UpdateAccountRequest.serializer(), withAvatar).contains("url"))
        assertFalse(json.encodeToString(UpdateAccountRequest.serializer(), withoutAvatar).contains("avatarUrl"))
    }

    @Test
    fun `getAccountInfo returns mapped UserProfile on success`() = runTest {
        val accountJson = """
            {
              "id": "1",
              "username": "alice",
              "roles": ["ADMIN"],
              "fullName": "Alice Admin",
              "email": "alice@example.com",
              "phoneNumber": "+79990000000",
              "avatarUrl": null,
              "enabled": true
            }
        """.trimIndent()

        val engine = MockEngine {
            respond(
                content = accountJson,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        val result = repository.getAccountInfo()

        assertTrue(result.isSuccess)
        assertEquals(
            UserProfile(
                id = "1",
                username = "alice",
                fullName = "Alice Admin",
                email = "alice@example.com",
                phoneNumber = "+79990000000",
                avatarUrl = null,
                roles = listOf("ADMIN"),
                enabled = true
            ),
            result.getOrNull()
        )
    }

    @Test
    fun `getAccountInfo returns failure on HTTP error`() = runTest {
        val engine = MockEngine {
            respond(content = "error", status = HttpStatusCode.InternalServerError)
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getAccountInfo().isFailure)
    }

    @Test
    fun `getAccountInfo returns failure on invalid JSON`() = runTest {
        val engine = MockEngine {
            respond(content = "not-json", status = HttpStatusCode.OK)
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getAccountInfo().isFailure)
    }

    @Test
    fun `updateAccount returns success when CSRF and POST succeed`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(
                    content = csrfJson(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
                else -> respond(content = "{}", status = HttpStatusCode.OK)
            }
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        val result = repository.updateAccount("Name", "mail@test.com", "+7", null)

        assertTrue(result.isSuccess)
        assertEquals(2, requestCount)
    }

    @Test
    fun `updateAccount returns failure when CSRF fails`() = runTest {
        val engine = MockEngine {
            respond(content = "forbidden", status = HttpStatusCode.Forbidden)
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.updateAccount("N", "e@e.com", "+7", null).isFailure)
    }

    @Test
    fun `updateAccount returns failure when POST returns non-OK`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(
                    content = csrfJson(),
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
                else -> respond(content = "bad", status = HttpStatusCode.BadRequest)
            }
        }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        val result = repository.updateAccount("Name", "mail@test.com", "+7", "avatar")

        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull()?.message)
    }

    @Test
    fun `updateAccount returns failure on network exception`() = runTest {
        val engine = MockEngine { throw RuntimeException("network down") }
        val repository = ProfileRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.updateAccount("N", "e@e.com", "+7", null).isFailure)
    }

    @Test
    fun `CsrfResponse round trip used by updateAccount path`() {
        val csrf = CsrfResponse(token = "abc", headerName = "X-CSRF", parameterName = "_csrf")
        val encoded = json.encodeToString(CsrfResponse.serializer(), csrf)
        val decoded = json.decodeFromString(CsrfResponse.serializer(), encoded)
        assertEquals(csrf, decoded)
    }
}
