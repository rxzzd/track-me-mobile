package com.example.track_me_mobile.features.meetings.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class MeetingDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Test
    fun `MeetingDto should serialize correctly`() {
        val dto = MeetingDto(
            id = "1",
            link = "https://meet",
            number = "12",
            startDate = "2025-01-01",
            teamStatus = "OK",
            status = "DONE",
            teamCardId = "team-1",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next"
        )

        val serialized = json.encodeToString(MeetingDto.serializer(), dto)

        assertTrue(serialized.contains("\"id\":\"1\""))
        assertTrue(serialized.contains("\"link\":\"https://meet\""))
    }

    @Test
    fun `MeetingDto should deserialize correctly`() {
        val rawJson = """
            {
                "id":"1",
                "link":"https://meet",
                "number":"12",
                "startDate":"2025-01-01",
                "teamStatus":"OK",
                "status":"DONE",
                "teamCardId":"team-1",
                "tasksCurrentMeeting":"current",
                "tasksNextMeeting":"next"
            }
        """.trimIndent()

        val dto = json.decodeFromString(MeetingDto.serializer(), rawJson)

        assertEquals("1", dto.id)
        assertEquals("https://meet", dto.link)
        assertEquals("DONE", dto.status)
    }

    @Test
    fun `MeetingDto copy should change only selected field`() {
        val dto = MeetingDto(
            id = "1",
            status = "OLD"
        )

        val updated = dto.copy(status = "NEW")

        assertEquals("1", updated.id)
        assertEquals("NEW", updated.status)
    }

    @Test
    fun `MeetingDto equals should work correctly`() {
        val first = MeetingDto(id = "1")
        val second = MeetingDto(id = "1")
        val third = MeetingDto(id = "2")

        assertEquals(first, second)
        assertNotEquals(first, third)
    }
}