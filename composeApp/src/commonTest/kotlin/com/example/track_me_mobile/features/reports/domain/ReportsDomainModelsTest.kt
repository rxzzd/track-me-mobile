package com.example.track_me_mobile.features.reports.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ReportsDomainModelsTest {

    @Test
    fun `ReportItem copy preserves unchanged fields`() {
        val item = ReportItem(
            streamName = "Stream",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            teamCardName = "Team",
            username = "tracker",
            averageTeamGrade = 4.0,
            averageUserGrade = 3.5,
            meetingsCountPlan = 10,
            meetingsCountFact = 8,
            ntiMarkets = listOf("M1"),
            readinessLevel = "5"
        )
        assertEquals("New Team", item.copy(teamCardName = "New Team").teamCardName)
        assertEquals(4.0, item.averageTeamGrade)
    }

    @Test
    fun `StreamMeetingReportItem holds nullable tracker fields`() {
        val item = StreamMeetingReportItem(
            teamName = "Alpha",
            trackerName = "nick",
            trackerFullName = null,
            teamStatus = "OK"
        )
        assertEquals("nick", item.trackerName)
        assertEquals("Alpha", item.teamName)
    }
}
