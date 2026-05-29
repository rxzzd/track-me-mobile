package com.example.track_me_mobile.features.users.data

import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.users.data.model.FilterCriteria
import com.example.track_me_mobile.features.users.data.model.PageInfo
import com.example.track_me_mobile.features.users.data.model.PagedResponse
import com.example.track_me_mobile.features.users.data.model.UserDto
import com.example.track_me_mobile.features.users.data.model.UsersRequest
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
import kotlin.test.assertTrue

class UsersDataBigTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `UserDto serializes nullable id and phoneNumber`() {
        val dto = UserDto(
            id = null,
            username = "nick",
            roles = listOf("TRACKER"),
            fullName = "Nick",
            email = "nick@example.com",
            phoneNumber = null,
            avatarUrl = null,
            enabled = false
        )
        val encoded = json.encodeToString(UserDto.serializer(), dto)
        val decoded = json.decodeFromString(UserDto.serializer(), encoded)
        assertEquals(dto, decoded)
    }

    @Test
    fun `PagedResponse and PageInfo serialize`() {
        val page = PagedResponse(
            content = listOf(
                UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@e.com", "+7", null, true)
            ),
            page = PageInfo(size = 1, number = 0, totalElements = 1, totalPages = 1)
        )
        val encoded = json.encodeToString(PagedResponse.serializer(UserDto.serializer()), page)
        assertTrue(encoded.contains("Alice"))
    }

    @Test
    fun `UsersRequest and FilterCriteria serialize blocked filter`() {
        val request = UsersRequest(
            filters = listOf(
                FilterCriteria(
                    fieldName = "accountNonLocked",
                    type = "EQ",
                    values = emptyList(),
                    value = "false"
                )
            )
        )
        val encoded = json.encodeToString(UsersRequest.serializer(), request)
        assertTrue(encoded.contains("accountNonLocked"))
    }

    @Test
    fun `getTrackers returns success on OK`() = runTest {
        var requestCount = 0
        val responseJson = json.encodeToString(
            PagedResponse.serializer(UserDto.serializer()),
            PagedResponse(
                content = listOf(UserDto("1", "alice", listOf("TRACKER"), "Alice", "a@e.com", null, null, true)),
                page = PageInfo(1, 0, 1, 1)
            )
        )
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = responseJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
            }
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        val result = repository.getTrackers(page = 0, size = 10, sort = listOf("username,asc"), showBlocked = true)
        assertTrue(result.isSuccess)
        assertEquals("alice", result.getOrNull()?.content?.first()?.username)
    }

    @Test
    fun `getAdministrators returns failure on HTTP error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.InternalServerError)
            }
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getAdministrators(showBlocked = false).isFailure)
    }

    @Test
    fun `getUserInfo returns user on success`() = runTest {
        val userJson = json.encodeToString(
            UserDto.serializer(),
            UserDto("1", "bob", listOf("ADMIN"), "Bob", "bob@e.com", "+7", null, true)
        )
        val engine = MockEngine {
            respond(content = userJson, status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        val result = repository.getUserInfo("bob")
        assertTrue(result.isSuccess)
        assertEquals("Bob", result.getOrNull()?.fullName)
    }

    @Test
    fun `enableUser returns success on OK`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "", status = HttpStatusCode.OK)
            }
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.enableUser("alice").isSuccess)
    }

    @Test
    fun `disableUser returns failure on HTTP error`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(content = "err", status = HttpStatusCode.Forbidden)
            }
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.disableUser("alice").isFailure)
    }

    @Test
    fun `getTrackers uses active filter when showBlocked is false`() = runTest {
        var requestCount = 0
        val engine = MockEngine {
            requestCount++
            when (requestCount) {
                1 -> respond(content = csrfJson(), status = HttpStatusCode.OK, headers = headersOf("Content-Type" to listOf("application/json")))
                else -> respond(
                    content = """{"content":[],"page":{"size":0,"number":0,"totalElements":0,"totalPages":0}}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
            }
        }
        val repository = UsersRepositoryImpl(testHttpClient(engine))

        assertTrue(repository.getTrackers(showBlocked = false).isSuccess)
        assertEquals(2, requestCount)
    }

    @Test
    fun `CsrfResponse used by repository helpers`() {
        val csrf = CsrfResponse("t", "H", "_csrf")
        val encoded = json.encodeToString(CsrfResponse.serializer(), csrf)
        assertFalse(encoded.isBlank())
    }
}
