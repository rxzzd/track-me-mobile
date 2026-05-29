package com.example.track_me_mobile.features.meetings.data.models

import kotlin.test.Test
import kotlin.test.assertEquals

class MeetingPageDtoTest {

    @Test
    fun `MeetingPageDto should contain meetings and page meta`() {
        val meeting = MeetingDto(id = "1")

        val page = PageMetaDto(
            size = 10,
            number = 0,
            totalElements = 1,
            totalPages = 1
        )

        val dto = MeetingPageDto(
            content = listOf(meeting),
            page = page
        )

        assertEquals(1, dto.content.size)
        assertEquals("1", dto.content.first().id)
        assertEquals(10, dto.page.size)
    }
}