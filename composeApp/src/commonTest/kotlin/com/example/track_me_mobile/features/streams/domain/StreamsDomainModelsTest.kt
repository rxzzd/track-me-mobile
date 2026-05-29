package com.example.track_me_mobile.features.streams.domain.models

import com.example.track_me_mobile.features.streams.domain.StreamPage
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamsDomainModelsTest {

    @Test
    fun `Stream and NtiMarket data classes`() {
        val market = NtiMarket("m1", "internal", "Display")
        val stream = Stream(
            id = "s1",
            name = "Stream",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            description = "D",
            active = true,
            trackStartDate = "2024-02-01",
            meetingsCount = 5,
            ntiMarkets = listOf(market)
        )
        assertEquals("New", stream.copy(name = "New").name)
        assertEquals("Display", stream.ntiMarkets.first().displayName)
    }

    @Test
    fun `StreamFilter and StreamCreateRequest defaults`() {
        val filter = StreamFilter("year", "EQ", "2024")
        val request = StreamCreateRequest(
            name = "N",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            ntiMarketIds = listOf("m1"),
            trackStartDate = "2024-02-01"
        )
        assertEquals("", request.description)
        assertEquals(0, request.meetingsCount)
        assertEquals("year", filter.fieldName)
    }

    @Test
    fun `StreamPage holds pagination metadata`() {
        val page = StreamPage(
            content = emptyList(),
            totalPages = 3,
            totalElements = 30,
            currentPage = 1
        )
        assertEquals(3, page.totalPages)
        assertEquals(1, page.currentPage)
    }
}
