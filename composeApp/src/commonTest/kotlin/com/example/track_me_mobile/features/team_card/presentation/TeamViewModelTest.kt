package com.example.track_me_mobile.features.team_card.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TeamViewModelTest {

    @Test
    fun `TeamViewModel initial state has default TeamFilterData`() {
        val viewModel = TeamViewModel()
        val data = viewModel.teamData.value
        assertEquals("", data.stream)
        assertEquals(emptyList<String>(), data.markets)
        assertEquals("", data.trl)
        assertEquals("", data.description)
        assertEquals("", data.trackerName)
    }

    @Test
    fun `TeamViewModel updateTrackerName sets tracker name`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker1")
        assertEquals("tracker1", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel updateTrackerName multiple times`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("first")
        assertEquals("first", viewModel.teamData.value.trackerName)
        viewModel.updateTrackerName("second")
        assertEquals("second", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel updateTrackerName empty string`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("")
        assertEquals("", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel updateStream sets stream`() {
        val viewModel = TeamViewModel()
        viewModel.updateStream("Stream 1")
        assertEquals("Stream 1", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel updateStream multiple times`() {
        val viewModel = TeamViewModel()
        viewModel.updateStream("S1")
        assertEquals("S1", viewModel.teamData.value.stream)
        viewModel.updateStream("S2")
        assertEquals("S2", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel updateStream empty string`() {
        val viewModel = TeamViewModel()
        viewModel.updateStream("")
        assertEquals("", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel updateMarkets sets markets`() {
        val viewModel = TeamViewModel()
        viewModel.updateMarkets(listOf("M1", "M2"))
        assertEquals(listOf("M1", "M2"), viewModel.teamData.value.markets)
    }

    @Test
    fun `TeamViewModel updateMarkets empty list`() {
        val viewModel = TeamViewModel()
        viewModel.updateMarkets(listOf("M1"))
        viewModel.updateMarkets(emptyList())
        assertTrue(viewModel.teamData.value.markets.isEmpty())
    }

    @Test
    fun `TeamViewModel updateMarkets single market`() {
        val viewModel = TeamViewModel()
        viewModel.updateMarkets(listOf("Only Market"))
        assertEquals(1, viewModel.teamData.value.markets.size)
        assertEquals("Only Market", viewModel.teamData.value.markets[0])
    }

    @Test
    fun `TeamViewModel updateMarkets many markets`() {
        val viewModel = TeamViewModel()
        val markets = (1..50).map { "M$it" }
        viewModel.updateMarkets(markets)
        assertEquals(50, viewModel.teamData.value.markets.size)
    }

    @Test
    fun `TeamViewModel updateTrl sets trl`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrl("3-5")
        assertEquals("3-5", viewModel.teamData.value.trl)
    }

    @Test
    fun `TeamViewModel updateTrl multiple values`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrl("0-2")
        assertEquals("0-2", viewModel.teamData.value.trl)
        viewModel.updateTrl("9-10")
        assertEquals("9-10", viewModel.teamData.value.trl)
    }

    @Test
    fun `TeamViewModel updateTrl empty string`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrl("")
        assertEquals("", viewModel.teamData.value.trl)
    }

    @Test
    fun `TeamViewModel updateDescription sets description`() {
        val viewModel = TeamViewModel()
        viewModel.updateDescription("Test description")
        assertEquals("Test description", viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel updateDescription multiple times`() {
        val viewModel = TeamViewModel()
        viewModel.updateDescription("first")
        assertEquals("first", viewModel.teamData.value.description)
        viewModel.updateDescription("second")
        assertEquals("second", viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel updateDescription empty string`() {
        val viewModel = TeamViewModel()
        viewModel.updateDescription("")
        assertEquals("", viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel updateDescription long string`() {
        val viewModel = TeamViewModel()
        val longDesc = "A".repeat(5000)
        viewModel.updateDescription(longDesc)
        assertEquals(longDesc, viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel applyEdit replaces entire state`() {
        val viewModel = TeamViewModel()
        val newData = TeamFilterData(
            stream = "S1",
            markets = listOf("M1"),
            trl = "6-8",
            description = "desc",
            trackerName = "tracker"
        )
        viewModel.applyEdit(newData)
        assertEquals("S1", viewModel.teamData.value.stream)
        assertEquals(listOf("M1"), viewModel.teamData.value.markets)
        assertEquals("6-8", viewModel.teamData.value.trl)
        assertEquals("desc", viewModel.teamData.value.description)
        assertEquals("tracker", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel applyEdit with default data`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("old")
        viewModel.updateStream("old-stream")

        viewModel.applyEdit(TeamFilterData())
        assertEquals("", viewModel.teamData.value.trackerName)
        assertEquals("", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel reset restores defaults`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker")
        viewModel.updateStream("stream")
        viewModel.updateMarkets(listOf("M1"))
        viewModel.updateTrl("3-5")
        viewModel.updateDescription("desc")

        viewModel.reset()
        assertEquals("", viewModel.teamData.value.trackerName)
        assertEquals("", viewModel.teamData.value.stream)
        assertTrue(viewModel.teamData.value.markets.isEmpty())
        assertEquals("", viewModel.teamData.value.trl)
        assertEquals("", viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel reset after partial updates`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker")
        viewModel.reset()
        assertEquals("", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel reset twice`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("t1")
        viewModel.reset()
        viewModel.updateTrackerName("t2")
        viewModel.reset()
        assertEquals("", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel chained updates`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker")
        viewModel.updateStream("stream")
        viewModel.updateMarkets(listOf("M1", "M2"))
        viewModel.updateTrl("9-10")
        viewModel.updateDescription("Full description")

        val data = viewModel.teamData.value
        assertEquals("tracker", data.trackerName)
        assertEquals("stream", data.stream)
        assertEquals(listOf("M1", "M2"), data.markets)
        assertEquals("9-10", data.trl)
        assertEquals("Full description", data.description)
    }

    @Test
    fun `TeamViewModel updateMarkets preserves other fields`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker")
        viewModel.updateStream("stream")
        viewModel.updateMarkets(listOf("M1"))
        assertEquals("tracker", viewModel.teamData.value.trackerName)
        assertEquals("stream", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel updateTrl preserves other fields`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("tracker")
        viewModel.updateTrl("3-5")
        assertEquals("tracker", viewModel.teamData.value.trackerName)
        assertEquals("3-5", viewModel.teamData.value.trl)
    }

    @Test
    fun `TeamViewModel updateDescription preserves other fields`() {
        val viewModel = TeamViewModel()
        viewModel.updateStream("stream")
        viewModel.updateDescription("desc")
        assertEquals("stream", viewModel.teamData.value.stream)
        assertEquals("desc", viewModel.teamData.value.description)
    }

    @Test
    fun `TeamViewModel applyEdit with same data`() {
        val viewModel = TeamViewModel()
        val data = TeamFilterData(stream = "S1", trl = "3-5")
        viewModel.applyEdit(data)
        viewModel.applyEdit(data)
        assertEquals("S1", viewModel.teamData.value.stream)
        assertEquals("3-5", viewModel.teamData.value.trl)
    }

    @Test
    fun `TeamViewModel state is independent between instances`() {
        val vm1 = TeamViewModel()
        val vm2 = TeamViewModel()
        vm1.updateTrackerName("vm1-tracker")
        vm2.updateTrackerName("vm2-tracker")
        assertEquals("vm1-tracker", vm1.teamData.value.trackerName)
        assertEquals("vm2-tracker", vm2.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel updateMarkets with duplicate values`() {
        val viewModel = TeamViewModel()
        viewModel.updateMarkets(listOf("M1", "M1", "M1"))
        assertEquals(3, viewModel.teamData.value.markets.size)
        assertEquals("M1", viewModel.teamData.value.markets[0])
        assertEquals("M1", viewModel.teamData.value.markets[1])
        assertEquals("M1", viewModel.teamData.value.markets[2])
    }

    @Test
    fun `TeamViewModel teamData is not null`() {
        val viewModel = TeamViewModel()
        assertNotNull(viewModel.teamData)
        assertNotNull(viewModel.teamData.value)
    }

    @Test
    fun `TeamViewModel updateStream with special characters`() {
        val viewModel = TeamViewModel()
        viewModel.updateStream("Stream №1 (2024) / Осень")
        assertEquals("Stream №1 (2024) / Осень", viewModel.teamData.value.stream)
    }

    @Test
    fun `TeamViewModel updateTrackerName with special characters`() {
        val viewModel = TeamViewModel()
        viewModel.updateTrackerName("Иванов Иван Иванович")
        assertEquals("Иванов Иван Иванович", viewModel.teamData.value.trackerName)
    }

    @Test
    fun `TeamViewModel updateMarkets with special characters`() {
        val viewModel = TeamViewModel()
        viewModel.updateMarkets(listOf("Рынок НТИ/AutoNet", "Рынок НТИ/MariNet"))
        assertEquals(2, viewModel.teamData.value.markets.size)
        assertEquals("Рынок НТИ/AutoNet", viewModel.teamData.value.markets[0])
        assertEquals("Рынок НТИ/MariNet", viewModel.teamData.value.markets[1])
    }
}