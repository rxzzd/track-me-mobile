package com.example.track_me_mobile.features.team_card.presentation

import cafe.adriel.voyager.core.screen.Screen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TeamScreensAndStateSmokeTest {

    @Test
    fun `team screen route classes keep ids and behave as screens`() {
        val info = InfoTeamLevel("team-1")
        val infoCopy = info.copy(teamId = "team-2")
        val edit = EditTeamLevel("team-1")
        val meetings = MeetingsTeamLevel("team-1")
        val create = CreateTeamLevel()
        val filter = FilterTeamLevel(TeamViewModel())

        assertEquals("team-1", info.teamId)
        assertEquals("team-2", infoCopy.teamId)
        assertEquals("team-1", edit.teamId)
        assertEquals("team-1", meetings.teamId)
        assertTrue(info is Screen)
        assertTrue(edit is Screen)
        assertTrue(meetings is Screen)
        assertTrue(create is Screen)
        assertTrue(filter is Screen)
        assertNotEquals(info, infoCopy)
        assertTrue(info.toString().contains("team-1"))
        assertTrue(edit.toString().contains("team-1"))
        assertTrue(meetings.toString().contains("team-1"))
    }

    @Test
    fun `team filter data copies every field`() {
        val base = TeamFilterData()
        val updated = base.copy(
            stream = "Stream A",
            markets = listOf("AeroNet", "HealthNet"),
            trl = "3-5",
            description = "Description",
            trackerName = "Tracker"
        )

        assertEquals("", base.stream)
        assertEquals(emptyList(), base.markets)
        assertEquals("Stream A", updated.stream)
        assertEquals(listOf("AeroNet", "HealthNet"), updated.markets)
        assertEquals("3-5", updated.trl)
        assertEquals("Description", updated.description)
        assertEquals("Tracker", updated.trackerName)
        assertTrue(updated.toString().contains("Stream A"))
    }

    @Test
    fun `team view model updates and resets all filters`() {
        val viewModel = TeamViewModel()

        viewModel.updateStream("Stream B")
        viewModel.updateMarkets(listOf("AutoNet"))
        viewModel.updateTrl("6-8")
        viewModel.updateDescription("Big description")
        viewModel.updateTrackerName("Alice")

        assertEquals(
            TeamFilterData(
                stream = "Stream B",
                markets = listOf("AutoNet"),
                trl = "6-8",
                description = "Big description",
                trackerName = "Alice"
            ),
            viewModel.teamData.value
        )

        val replacement = TeamFilterData(
            stream = "Stream C",
            markets = listOf("AeroNet"),
            trl = "9-10",
            description = "Replacement",
            trackerName = "Bob"
        )
        viewModel.applyEdit(replacement)
        assertEquals(replacement, viewModel.teamData.value)

        viewModel.reset()
        assertEquals(TeamFilterData(), viewModel.teamData.value)
    }

    @Test
    fun `meeting data is a simple copyable value`() {
        val meeting = MeetingData(date = "01.06", title = "Meeting 1")
        val changed = meeting.copy(title = "Meeting 2")

        assertEquals("01.06", meeting.date)
        assertEquals("Meeting 1", meeting.title)
        assertEquals("Meeting 2", changed.title)
        assertTrue(changed.toString().contains("Meeting 2"))
    }
}
