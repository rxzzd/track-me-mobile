package com.example.track_me_mobile.features.team_card.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TeamComponentsDataTest {

    @Test
    fun `TeamFilterData default values are empty`() {
        val data = TeamFilterData()
        assertEquals("", data.stream)
        assertEquals(emptyList<String>(), data.markets)
        assertEquals("", data.trl)
        assertEquals("", data.description)
        assertEquals("", data.trackerName)
    }

    @Test
    fun `TeamFilterData copy with stream`() {
        val data = TeamFilterData().copy(stream = "Stream 1")
        assertEquals("Stream 1", data.stream)
        assertEquals("", data.trl)
    }

    @Test
    fun `TeamFilterData copy with markets`() {
        val data = TeamFilterData().copy(markets = listOf("Market A", "Market B"))
        assertEquals(listOf("Market A", "Market B"), data.markets)
        assertEquals(2, data.markets.size)
    }

    @Test
    fun `TeamFilterData copy with trl`() {
        val data = TeamFilterData().copy(trl = "3-5")
        assertEquals("3-5", data.trl)
    }

    @Test
    fun `TeamFilterData copy with description`() {
        val data = TeamFilterData().copy(description = "Test description")
        assertEquals("Test description", data.description)
    }

    @Test
    fun `TeamFilterData copy with trackerName`() {
        val data = TeamFilterData().copy(trackerName = "tracker1")
        assertEquals("tracker1", data.trackerName)
    }

    @Test
    fun `TeamFilterData copy with all fields`() {
        val data = TeamFilterData().copy(
            stream = "Stream 1",
            markets = listOf("M1", "M2"),
            trl = "6-8",
            description = "Full description",
            trackerName = "tracker"
        )
        assertEquals("Stream 1", data.stream)
        assertEquals(listOf("M1", "M2"), data.markets)
        assertEquals("6-8", data.trl)
        assertEquals("Full description", data.description)
        assertEquals("tracker", data.trackerName)
    }

    @Test
    fun `TeamFilterData multiple copies are independent`() {
        val original = TeamFilterData()
        val copy1 = original.copy(stream = "S1")
        val copy2 = original.copy(stream = "S2")
        assertEquals("S1", copy1.stream)
        assertEquals("S2", copy2.stream)
        assertEquals("", original.stream)
    }

    @Test
    fun `TeamFilterData toString contains fields`() {
        val data = TeamFilterData(stream = "S1", trl = "3-5")
        val str = data.toString()
        assertNotNull(str)
        assertTrue(str.contains("stream"))
        assertTrue(str.contains("trl"))
    }

    @Test
    fun `TeamFilterData equals same values`() {
        val a = TeamFilterData(stream = "S1", markets = listOf("M1"))
        val b = TeamFilterData(stream = "S1", markets = listOf("M1"))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `TeamFilterData equals different values`() {
        val a = TeamFilterData(stream = "S1")
        val b = TeamFilterData(stream = "S2")
        assertTrue(a != b)
    }

    @Test
    fun `MeetingData default constructor`() {
        val data = MeetingData(date = "01.01.2024", title = "Meeting 1")
        assertEquals("01.01.2024", data.date)
        assertEquals("Meeting 1", data.title)
    }

    @Test
    fun `MeetingData copy with new date`() {
        val data = MeetingData(date = "01.01.2024", title = "Meeting 1")
        val copy = data.copy(date = "15.03.2024")
        assertEquals("15.03.2024", copy.date)
        assertEquals("Meeting 1", copy.title)
    }

    @Test
    fun `MeetingData copy with new title`() {
        val data = MeetingData(date = "01.01.2024", title = "Meeting 1")
        val copy = data.copy(title = "Meeting 2")
        assertEquals("01.01.2024", copy.date)
        assertEquals("Meeting 2", copy.title)
    }

    @Test
    fun `MeetingData equals same values`() {
        val a = MeetingData(date = "01.01.2024", title = "M1")
        val b = MeetingData(date = "01.01.2024", title = "M1")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `MeetingData equals different values`() {
        val a = MeetingData(date = "01.01.2024", title = "M1")
        val b = MeetingData(date = "02.02.2024", title = "M2")
        assertTrue(a != b)
    }

    @Test
    fun `MeetingData toString contains fields`() {
        val data = MeetingData(date = "01.01.2024", title = "M1")
        val str = data.toString()
        assertNotNull(str)
        assertTrue(str.contains("date"))
        assertTrue(str.contains("title"))
    }

    @Test
    fun `TeamFilterData empty markets list`() {
        val data = TeamFilterData(markets = emptyList())
        assertTrue(data.markets.isEmpty())
    }

    @Test
    fun `TeamFilterData single market`() {
        val data = TeamFilterData(markets = listOf("Only Market"))
        assertEquals(1, data.markets.size)
        assertEquals("Only Market", data.markets[0])
    }

    @Test
    fun `TeamFilterData many markets`() {
        val markets = (1..100).map { "Market $it" }
        val data = TeamFilterData(markets = markets)
        assertEquals(100, data.markets.size)
        assertEquals("Market 1", data.markets[0])
        assertEquals("Market 100", data.markets[99])
    }

    @Test
    fun `TeamFilterData special characters in fields`() {
        val data = TeamFilterData(
            stream = "Stream №1 (2024)",
            markets = listOf("Рынок НТИ/AutoNet"),
            trl = "9-10",
            description = "Описание команды с пробелами!",
            trackerName = "Иванов И.И."
        )
        assertEquals("Stream №1 (2024)", data.stream)
        assertEquals("Рынок НТИ/AutoNet", data.markets[0])
        assertEquals("9-10", data.trl)
        assertEquals("Описание команды с пробелами!", data.description)
        assertEquals("Иванов И.И.", data.trackerName)
    }

    @Test
    fun `MeetingData empty strings`() {
        val data = MeetingData(date = "", title = "")
        assertEquals("", data.date)
        assertEquals("", data.title)
    }

    @Test
    fun `MeetingData long strings`() {
        val longDate = "01." + "01.".repeat(50)
        val longTitle = "M".repeat(1000)
        val data = MeetingData(date = longDate, title = longTitle)
        assertEquals(longDate, data.date)
        assertEquals(longTitle, data.title)
    }

    @Test
    fun `TeamFilterData component1 returns stream`() {
        val data = TeamFilterData(stream = "S1")
        assertEquals("S1", data.component1())
    }

    @Test
    fun `TeamFilterData component2 returns markets`() {
        val data = TeamFilterData(markets = listOf("M1"))
        assertEquals(listOf("M1"), data.component2())
    }

    @Test
    fun `TeamFilterData component3 returns trl`() {
        val data = TeamFilterData(trl = "3-5")
        assertEquals("3-5", data.component3())
    }

    @Test
    fun `TeamFilterData component4 returns description`() {
        val data = TeamFilterData(description = "desc")
        assertEquals("desc", data.component4())
    }

    @Test
    fun `TeamFilterData component5 returns trackerName`() {
        val data = TeamFilterData(trackerName = "tracker")
        assertEquals("tracker", data.component5())
    }

    @Test
    fun `MeetingData component1 returns date`() {
        val data = MeetingData(date = "01.01", title = "M1")
        assertEquals("01.01", data.component1())
    }

    @Test
    fun `MeetingData component2 returns title`() {
        val data = MeetingData(date = "01.01", title = "M1")
        assertEquals("M1", data.component2())
    }

    @Test
    fun `TeamFilterData destructuring declaration`() {
        val data = TeamFilterData(stream = "S1", markets = listOf("M1"), trl = "3-5", description = "desc", trackerName = "t")
        val (stream, markets, trl, description, trackerName) = data
        assertEquals("S1", stream)
        assertEquals(listOf("M1"), markets)
        assertEquals("3-5", trl)
        assertEquals("desc", description)
        assertEquals("t", trackerName)
    }

    @Test
    fun `MeetingData destructuring declaration`() {
        val data = MeetingData(date = "01.01.2024", title = "Meeting 1")
        val (date, title) = data
        assertEquals("01.01.2024", date)
        assertEquals("Meeting 1", title)
    }
}