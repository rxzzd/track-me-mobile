package com.example.track_me_mobile.features.meetings.data.models

import kotlin.test.Test
import kotlin.test.assertEquals

class PageMetaDtoTest {

    @Test
    fun `PageMetaDto should store values correctly`() {
        val dto = PageMetaDto(
            size = 20,
            number = 1,
            totalElements = 100,
            totalPages = 5
        )

        assertEquals(20, dto.size)
        assertEquals(1, dto.number)
        assertEquals(100, dto.totalElements)
        assertEquals(5, dto.totalPages)
    }

    @Test
    fun `PageMetaDto copy should work`() {
        val dto = PageMetaDto(
            size = 20,
            number = 1,
            totalElements = 100,
            totalPages = 5
        )

        val copied = dto.copy(number = 2)

        assertEquals(2, copied.number)
        assertEquals(20, copied.size)
    }
}