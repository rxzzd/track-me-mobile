package com.example.track_me_mobile.features.team_card.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TeamCardDomainModelsTest {

    @Test
    fun `CreateTeamRequest holds all create fields`() {
        val request = CreateTeamRequest(
            name = "Team",
            meetingRoomLink = "https://link.com",
            description = "Description",
            trackerUsername = "tracker",
            streamId = "s1",
            ntiMarketIds = listOf("m1"),
            readinessLevel = "5-7"
        )
        assertEquals("New", request.copy(name = "New").name)
    }

    @Test
    fun `UpdateTeamRequest optional admin fields default to null`() {
        val request = UpdateTeamRequest(
            teamId = "t1",
            name = "Team",
            meetingRoomLink = "link",
            description = "desc",
            ntiMarketIds = listOf("m1"),
            readinessLevel = "5"
        )
        assertNull(request.trackerUsername)
        assertNull(request.streamId)
    }

    @Test
    fun `TrackerUser data class`() {
        val user = TrackerUser("1", "nick", "Nick", "n@e.com", null)
        assertEquals("nick", user.username)
    }
}
