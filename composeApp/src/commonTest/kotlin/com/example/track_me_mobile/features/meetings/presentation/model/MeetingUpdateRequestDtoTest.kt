package com.example.track_me_mobile.features.meetings.data.models

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeetingUpdateRequestDtoTest {

    private val json = Json

    @Test
    fun `MeetingUpdateRequestDto should serialize correctly`() {
        val dto = MeetingUpdateRequestDto(
            id = "1",
            link = "https://meet",
            number = "10",
            teamStatus = "OK",
            tasksCurrentMeeting = "current",
            tasksNextMeeting = "next",
            startDate = "2025-01-01",
            status = "DONE",
            teamCardId = "team-1"
        )

        val serialized = json.encodeToString(
            MeetingUpdateRequestDto.serializer(),
            dto
        )

        assertTrue(serialized.contains("\"id\":\"1\""))
        assertTrue(serialized.contains("\"status\":\"DONE\""))
    }

    @Test
    fun `MeetingUpdateRequestDto should deserialize correctly`() {
        val rawJson = """
            {
                "id":"1",
                "link":"https://meet",
                "number":"10",
                "teamStatus":"OK",
                "tasksCurrentMeeting":"current",
                "tasksNextMeeting":"next",
                "startDate":"2025-01-01",
                "status":"DONE",
                "teamCardId":"team-1"
            }
        """.trimIndent()

        val dto = Json.decodeFromString(
            MeetingUpdateRequestDto.serializer(),
            rawJson
        )

        assertEquals("1", dto.id)
        assertEquals("DONE", dto.status)
        assertEquals("team-1", dto.teamCardId)
    }
}