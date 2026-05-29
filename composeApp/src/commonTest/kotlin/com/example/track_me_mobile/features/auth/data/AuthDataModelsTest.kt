package com.example.track_me_mobile.features.auth.data

import com.example.track_me_mobile.features.auth.data.model.CsrfResponse
import com.example.track_me_mobile.features.auth.data.model.RegistrationRequest
import com.example.track_me_mobile.features.auth.data.model.UserInfoDto
import com.example.track_me_mobile.features.auth.data.AuthRepositoryImpl
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthDataModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `csrf response serialization roundtrip`() {
        val payload = """{"token":"abc","headerName":"X-CSRF-Token","parameterName":"_csrf"}"""
        val response = json.decodeFromString(CsrfResponse.serializer(), payload)

        assertEquals("abc", response.token)
        assertEquals("X-CSRF-Token", response.headerName)
        assertEquals("_csrf", response.parameterName)

        assertEquals(payload, json.encodeToString(CsrfResponse.serializer(), response))
    }

    @Test
    fun `registration request serialization roundtrip`() {
        val request = RegistrationRequest(
            username = "john_doe",
            password = "secret",
            phoneNumber = "+70000000000",
            fullName = "John Doe",
            email = "john.doe@example.com",
            role = "TRACKER"
        )

        val serialized = json.encodeToString(RegistrationRequest.serializer(), request)
        val deserialized = json.decodeFromString(RegistrationRequest.serializer(), serialized)

        assertEquals(request, deserialized)
    }

    @Test
    fun `user info dto handles optional fields`() {
        val payload = """{"username":"alice","roles":["TRACKER"],"enabled":true}"""
        val dto = json.decodeFromString(UserInfoDto.serializer(), payload)

        assertEquals("alice", dto.username)
        assertEquals(listOf("TRACKER"), dto.roles)
        assertNull(dto.id)
        assertNull(dto.fullName)
        assertNull(dto.email)
        assertNull(dto.phoneNumber)
        assertNull(dto.avatarUrl)
        assertEquals(true, dto.enabled)
    }

    @Test
    fun `auth repository impl class is available`() {
        val clazz = AuthRepositoryImpl::class
        assertNotNull(clazz)
        assertEquals("AuthRepositoryImpl", clazz.simpleName)
    }
}
